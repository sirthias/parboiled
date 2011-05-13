/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.parserunners;

import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Predicate;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchervisitors.DoWithMatcherVisitor;
import org.parboiled.support.ParsingResult;

import java.text.DecimalFormat;
import java.util.*;

import static org.parboiled.common.Preconditions.checkArgNotNull;
import static org.parboiled.common.Utils.humanize;

/**
 * <p>The ProfilingParseRunner is a special {@link ParseRunner} implementation that "watches" a parser digest a number
 * of inputs and collects all sorts of statistical data on the what rules have matched how many times, the number
 * of reincovations of rules at identical input locations, and so on.</p>
 * <p>The ProfilingParseRunner is typically used during parser debugging and optimization, not in production.</p>
 *
 * @param <V>
 */
public class ProfilingParseRunner<V> extends AbstractParseRunner<V> implements MatchHandler {
    private final Map<Rule, RuleReport> ruleReports = new HashMap<Rule, RuleReport>();
    private int runMatches;
    private int totalRuns;
    private int totalMatches;
    private int totalMismatches;
    private int totalRematches;
    private int totalRemismatches;
    private long totalNanoTime;
    private long timeCorrection;

    private final DoWithMatcherVisitor.Action updateStatsAction = new DoWithMatcherVisitor.Action() {
        public void process(Matcher matcher) {
            RuleStats ruleStats = (RuleStats) matcher.getTag();
            int rematches = 0, remismatches = 0;
            for (Integer i : ruleStats.positionMatches.values()) {
                if (i > 0) {
                    rematches += i - 1;
                } else if (i < 0) {
                    remismatches += -(i + 1);
                }
            }
            totalMatches += ruleStats.matches;
            totalMismatches += ruleStats.mismatches;
            totalRematches += rematches;
            totalRemismatches += remismatches;
            RuleReport ruleReport = ruleReports.get(matcher);
            if (ruleReport == null) {
                ruleReport = new RuleReport(matcher);
                ruleReports.put(matcher, ruleReport);
            }
            ruleReport.update(ruleStats.matches, ruleStats.matchSubs, ruleStats.mismatches, ruleStats.mismatchSubs,
                    rematches, ruleStats.rematchSubs, remismatches, ruleStats.remismatchSubs, ruleStats.nanoTime);
        }
    };

    /**
     * Creates a new ProfilingParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public ProfilingParseRunner(Rule rule) {
        super(rule);
    }

    public ParsingResult<V> run(InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        resetValueStack();
        totalRuns++;

        MatcherContext<V> rootContext = createRootContext(inputBuffer, this, true);
        rootContext.getMatcher().accept(new DoWithMatcherVisitor(new DoWithMatcherVisitor.Action() {
            public void process(Matcher matcher) {
                RuleStats ruleStats = (RuleStats) matcher.getTag();
                if (ruleStats == null) {
                    ruleStats = new RuleStats();
                    matcher.setTag(ruleStats);
                } else {
                    ruleStats.clear();
                }
            }
        }));

        runMatches = 0;
        long timeStamp = System.nanoTime() - timeCorrection;
        boolean matched = rootContext.runMatcher();
        totalNanoTime += System.nanoTime() - timeCorrection - timeStamp;

        getRootMatcher().accept(new DoWithMatcherVisitor(updateStatsAction));
        return createParsingResult(matched, rootContext);
    }

    public Report getReport() {
        return new Report(totalRuns, totalMatches, totalMismatches, totalRematches, totalRemismatches,
                totalNanoTime, new ArrayList<RuleReport>(ruleReports.values()));
    }

    public boolean match(MatcherContext<?> context) {
        long timeStamp = System.nanoTime();
        Matcher matcher = context.getMatcher();
        RuleStats ruleStats = ((RuleStats) matcher.getTag());
        int pos = context.getCurrentIndex();

        int subMatches = -++runMatches;
        int matchSubs = ruleStats.matchSubs;
        int rematchSubs = ruleStats.rematchSubs;
        int mismatchSubs = ruleStats.mismatchSubs;
        int remismatchSubs = ruleStats.remismatchSubs;

        long time = System.nanoTime();
        timeCorrection += time - timeStamp;
        timeStamp = time - timeCorrection;

        boolean matched = matcher.match(context);

        time = System.nanoTime();
        ruleStats.nanoTime += time - timeCorrection - timeStamp;
        timeStamp = time;

        subMatches += runMatches;

        Integer posMatches = ruleStats.positionMatches.get(pos);
        if (matched) {
            ruleStats.matches++;
            ruleStats.matchSubs = matchSubs + subMatches;
            if (posMatches == null) {
                posMatches = 1;
            } else if (posMatches > 0) {
                posMatches++;
                ruleStats.rematchSubs = rematchSubs + subMatches;
            } else if (posMatches < 0) {
                posMatches = 0;
            }
        } else {
            ruleStats.mismatches++;
            ruleStats.mismatchSubs = mismatchSubs + subMatches;
            if (posMatches == null) {
                posMatches = -1;
            } else if (posMatches < 0) {
                posMatches--;
                ruleStats.remismatchSubs = remismatchSubs + subMatches;
            } else if (posMatches > 0) {
                posMatches = 0;
            }
        }
        ruleStats.positionMatches.put(pos, posMatches);
        timeCorrection += System.nanoTime() - timeStamp;
        return matched;
    }

    private static class RuleStats {
        private int matches;
        private int mismatches;
        private int matchSubs;
        private int mismatchSubs;
        private int rematchSubs;
        private int remismatchSubs;
        private long nanoTime;

        // map Index -> matches at that position
        // no entry for a position means that the rule was never tried for that position
        // an entry n > 0 means that the rule matched n times
        // an entry n < 0 means that the rule failed n times
        // an entry of 0 for a position means that the rule matched as well as failed at the position (should happen
        // only for "strange" action rules)
        private final Map<Integer, Integer> positionMatches = new HashMap<Integer, Integer>();

        private void clear() {
            matches = 0;
            mismatches = 0;
            matchSubs = 0;
            mismatchSubs = 0;
            rematchSubs = 0;
            remismatchSubs = 0;
            nanoTime = 0;
            positionMatches.clear();
        }
    }

    public static class Report {
        private final static DecimalFormat fmt = new DecimalFormat("0.###");

        public static final Predicate<RuleReport> allRules = new Predicate<RuleReport>() {
            public boolean apply(RuleReport rep) {
                return true;
            }
        };

        public static final Predicate<RuleReport> namedRules = new Predicate<RuleReport>() {
            public boolean apply(RuleReport rep) {
                return rep.getMatcher().hasCustomLabel();
            }
        };

        public final int totalRuns;
        public final int totalInvocations;
        public final int totalMatches;
        public final int totalMismatches;
        public final double matchShare;
        public final int reinvocations;
        public final int rematches;
        public final int remismatches;
        public final double reinvocationShare;
        public final long totalNanoTime;
        public final List<RuleReport> ruleReports;

        public Report(int totalRuns, int totalMatches, int totalMismatches, int rematches, int remismatches,
                      long totalNanoTime, List<RuleReport> ruleReports) {
            this.totalRuns = totalRuns;
            this.totalInvocations = totalMatches + totalMismatches;
            this.totalMatches = totalMatches;
            this.totalMismatches = totalMismatches;
            this.matchShare = ((double) totalMatches) / totalInvocations;
            this.reinvocations = rematches + remismatches;
            this.rematches = rematches;
            this.remismatches = remismatches;
            this.reinvocationShare = ((double) reinvocations) / totalInvocations;
            this.totalNanoTime = totalNanoTime;
            this.ruleReports = ruleReports;
        }

        public String print() {
            StringBuilder sb = new StringBuilder();
            sb.append("Profiling Report\n");
            sb.append("----------------\n");
            sb.append(printBasics());
            sb.append("\n");
            sb.append("Top 20 named rules by invocations:\n");
            sb.append(sortByInvocations().printTopRules(20, namedRules));
            sb.append("\n");
            sb.append("Top 20 named rules by sub-invocations:\n");
            sb.append(sortBySubInvocations().printTopRules(20, namedRules));
            sb.append("\n");
            sb.append("Top 20 named rules by re-invocations:\n");
            sb.append(sortByReinvocations().printTopRules(20, namedRules));
            sb.append("\n");
            sb.append("Top 20 named rules by re-sub-invocations:\n");
            sb.append(sortByResubinvocations().printTopRules(20, namedRules));
            sb.append("\n");
            sb.append("Top 20 named rules by re-mismatches:\n");
            sb.append(sortByRemismatches().printTopRules(20, namedRules));
            sb.append("\n");
            sb.append("Top 20 named rules by re-sub-mismatches:\n");
            sb.append(sortByResubmismatches().printTopRules(20, namedRules));
            return sb.toString();
        }

        public String printBasics() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Runs                     : %,15d\n", totalRuns));
            sb.append(String.format("Active rules             : %,15d\n", ruleReports.size()));
            sb.append(String.format("Total net rule time      : %,15.3f s\n", totalNanoTime / 1000000000.0));
            sb.append(String.format("Total rule invocations   : %,15d\n", totalInvocations));
            sb.append(String.format("Total rule matches       : %,15d\n", totalMatches));
            sb.append(String.format("Total rule mismatches    : %,15d\n", totalMismatches));
            sb.append(String.format("Total match share        : %15.2f %%\n", 100.0 * matchShare));
            sb.append(String.format("Rule re-invocations      : %,15d\n", reinvocations));
            sb.append(String.format("Rule re-matches          : %,15d\n", rematches));
            sb.append(String.format("Rule re-mismatches       : %,15d\n", remismatches));
            sb.append(String.format("Rule re-invocation share : %15.2f %%\n", 100.0 * reinvocationShare));
            return sb.toString();
        }

        public String printTopRules(int count, Predicate<RuleReport> filter) {
            checkArgNotNull(filter, "filter");
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "Rule                           | Net-Time  |   Invocations   |     Matches     |   Mismatches    |   Time/Invoc.   | Match % |    Re-Invocs    |   Re-Matches    |   Re-Mismatch   |     Re-Invoc %    \n");
            sb.append(
                    "-------------------------------|-----------|-----------------|-----------------|-----------------|-----------------|---------|-----------------|-----------------|-----------------|-------------------\n");
            for (int i = 0; i < Math.min(ruleReports.size(), count); i++) {
                RuleReport rep = ruleReports.get(i);
                if (!filter.apply(rep)) {
                    count++;
                    continue;
                }
                sb.append(String.format(
                        "%-30s | %6.0f ms | %6s / %6s | %6s / %6s | %6s / %6s | %,12.0f ns | %6.2f%% | %6s / %6s | %6s / %6s | %6s / %6s | %6.2f%% / %6.2f%%\n",
                        StringUtils.left(
                                rep.getMatcher().toString() + ": " + rep.getMatcher().getClass().getSimpleName()
                                        .replace("Matcher", ""), 30),
                        rep.getNanoTime() / 1000000.0,
                        humanize(rep.getInvocations()), humanize(rep.getInvocationSubs()),
                        humanize(rep.getMatches()), humanize(rep.getMatchSubs()),
                        humanize(rep.getMismatches()), humanize(rep.getMismatchSubs()),
                        rep.getNanoTime() / (double) rep.getInvocations(),
                        rep.getMatchShare() * 100,
                        humanize(rep.getReinvocations()), humanize(rep.getReinvocationSubs()),
                        humanize(rep.getRematches()), humanize(rep.getRematchSubs()),
                        humanize(rep.getRemismatches()), humanize(rep.getRemismatchSubs()),
                        rep.getReinvocationShare() * 100, rep.getReinvocationShare2() * 100
                ));
            }
            return sb.toString();
        }

        public Report sortByInvocations() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getInvocations(), b.getInvocations());
                }
            });
            return this;
        }

        public Report sortBySubInvocations() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getInvocationSubs(), b.getInvocationSubs());
                }
            });
            return this;
        }

        public Report sortByTime() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return longCompare(a.getNanoTime(), b.getNanoTime());
                }
            });
            return this;
        }

        public Report sortByTimePerInvocation() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return doubleCompare(a.getNanoTime() / (double) a.getInvocations(),
                            b.getNanoTime() / (double) b.getInvocations());
                }
            });
            return this;
        }

        public Report sortByMatches() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getMatches(), b.getMatches());
                }
            });
            return this;
        }

        public Report sortByMismatches() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getMismatches(), b.getMismatches());
                }
            });
            return this;
        }

        public Report sortByReinvocations() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getReinvocations(), b.getReinvocations());
                }
            });
            return this;
        }

        public Report sortByResubinvocations() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return doubleCompare(a.getReinvocationSubs(), b.getReinvocationSubs());
                }
            });
            return this;
        }

        public Report sortByRematches() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getRematches(), b.getRematches());
                }
            });
            return this;
        }

        public Report sortByRemismatches() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getRemismatches(), b.getRemismatches());
                }
            });
            return this;
        }

        public Report sortByResubmismatches() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return doubleCompare(a.getRemismatchSubs(), b.getRemismatchSubs());
                }
            });
            return this;
        }

        private int intCompare(int a, int b) {
            return a < b ? 1 : a > b ? -1 : 0;
        }

        private int longCompare(long a, long b) {
            return a < b ? 1 : a > b ? -1 : 0;
        }

        private int doubleCompare(double a, double b) {
            return a < b ? 1 : a > b ? -1 : 0;
        }
    }

    public static class RuleReport {
        private final Matcher matcher;
        private int matches;
        private int matchSubs;
        private int mismatches;
        private int mismatchSubs;
        private int rematches;
        private int rematchSubs;
        private int remismatches;
        private int remismatchSubs;
        private long nanoTime;

        public RuleReport(Matcher matcher) {
            this.matcher = matcher;
        }

        public Matcher getMatcher() { return matcher; }

        public int getInvocations() { return matches + mismatches; }

        public int getInvocationSubs() { return matchSubs + mismatchSubs; }

        public int getMatches() { return matches; }

        public int getMatchSubs() { return matchSubs; }

        public int getMismatches() { return mismatches; }

        public int getMismatchSubs() { return mismatchSubs; }

        public double getMatchShare() { return ((double) matches) / getInvocations(); }

        public double getMatchShare2() { return ((double) matchSubs) / getInvocationSubs(); }

        public int getReinvocations() { return rematches + remismatches; }

        public int getReinvocationSubs() { return rematchSubs + remismatchSubs; }

        public int getRematches() { return rematches; }

        public int getRematchSubs() { return rematchSubs; }

        public int getRemismatches() { return remismatches; }

        public int getRemismatchSubs() { return remismatchSubs; }

        public double getReinvocationShare() { return ((double) getReinvocations()) / getInvocations(); }

        public double getReinvocationShare2() { return ((double) getReinvocationSubs()) / getInvocationSubs(); }

        public long getNanoTime() { return nanoTime; }

        public void update(int matchesDelta, int matchSubsDelta,
                           int mismatchesDelta, int mismatchSubsDelta,
                           int rematchesDelta, int rematchSubsDelta,
                           int remismatchesDelta, int remismatchSubsDelta,
                           long nanoTimeDelta) {
            matches += matchesDelta;
            matchSubs += matchSubsDelta;
            mismatches += mismatchesDelta;
            mismatchSubs += mismatchSubsDelta;
            rematches += rematchesDelta;
            rematchSubs += rematchSubsDelta;
            remismatches += remismatchesDelta;
            remismatchSubs += remismatchSubsDelta;
            nanoTime += nanoTimeDelta;
        }
    }
}