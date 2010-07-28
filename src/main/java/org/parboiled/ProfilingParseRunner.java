/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled;

import com.google.common.base.Predicate;
import org.jetbrains.annotations.NotNull;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.DoWithMatcherVisitor;
import org.parboiled.support.HasCustomLabelVisitor;
import org.parboiled.support.ValueStack;

import java.util.*;

public class ProfilingParseRunner<V> extends BasicParseRunner<V> {

    private final Map<Rule, RuleReport> ruleReports = new HashMap<Rule, RuleReport>();
    private int totalRuns;
    private int totalMatches;
    private int totalMismatches;
    private int totalRematches;
    private int totalRemismatches;
    private long totalNanoTime;

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
            ruleReport.update(ruleStats.matches, ruleStats.mismatches, rematches, remismatches, ruleStats.nanoTime);
        }
    };

    /**
     * Creates a new ProfilingParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public ProfilingParseRunner(@NotNull Rule rule) {
        super(rule);
    }

    /**
     * Creates a new ProfilingParseRunner instance for the given rule using the given ValueStack instance.
     *
     * @param rule       the parser rule
     * @param valueStack the value stack
     */
    public ProfilingParseRunner(@NotNull Rule rule, @NotNull ValueStack<V> valueStack) {
        super(rule, valueStack);
    }

    @Override
    protected boolean runRootContext() {
        totalRuns++;
        return runRootContext(new Handler(), true);
    }

    public Report getReport() {
        return new Report(totalRuns, totalMatches, totalMismatches, totalRematches, totalRemismatches,
                totalNanoTime, new ArrayList<RuleReport>(ruleReports.values()));
    }

    public class Handler implements MatchHandler {
        private long timeCorrection;

        public boolean matchRoot(MatcherContext<?> rootContext) {
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

            long timeStamp = System.nanoTime() - timeCorrection;
            boolean matched = rootContext.runMatcher();
            totalNanoTime += System.nanoTime() - timeCorrection - timeStamp;

            rootMatcher.accept(new DoWithMatcherVisitor(updateStatsAction));
            return matched;
        }

        public boolean match(MatcherContext<?> context) {
            long timeStamp = System.nanoTime();
            Matcher matcher = context.getMatcher();
            RuleStats ruleStats = ((RuleStats) matcher.getTag());
            int pos = context.getCurrentIndex();
            Integer posMatches = ruleStats.positionMatches.get(pos);

            long time = System.nanoTime();
            timeCorrection += time - timeStamp;
            timeStamp = time - timeCorrection;

            boolean matched = matcher.match(context);

            time = System.nanoTime();
            ruleStats.nanoTime += time - timeCorrection - timeStamp;
            timeStamp = time;

            if (matched) {
                ruleStats.matches++;
                if (posMatches == null) {
                    posMatches = 1;
                } else if (posMatches > 0) {
                    posMatches++;
                } else if (posMatches < 0) {
                    posMatches = 0;
                }
            } else {
                ruleStats.mismatches++;
                if (posMatches == null) {
                    posMatches = -1;
                } else if (posMatches < 0) {
                    posMatches--;
                } else if (posMatches > 0) {
                    posMatches = 0;
                }
            }
            ruleStats.positionMatches.put(pos, posMatches);
            timeCorrection += System.nanoTime() - timeStamp;
            return matched;
        }
    }

    private static class RuleStats {
        private int matches;
        private int mismatches;
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
            nanoTime = 0;
            positionMatches.clear();
        }
    }

    public static class Report {
        public static final Predicate<RuleReport> allRules = new Predicate<RuleReport>() {
            public boolean apply(RuleReport rep) {
                return true;
            }
        };

        public static final Predicate<RuleReport> namedRules = new Predicate<RuleReport>() {
            public boolean apply(RuleReport rep) {
                return rep.getMatcher().accept(new HasCustomLabelVisitor());
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

        public Report(int totalRuns, int totalMatches, int totalMismatches, int rematches,
                      int remismatches, long totalNanoTime, List<RuleReport> ruleReports) {
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
            sb.append("Top 20 named rules by time per invocation:\n");
            sb.append(sortByTimePerInvocation().printTopRules(20, namedRules));
            sb.append("\n");
            sb.append("Top 20 named rules by reinvocations:\n");
            sb.append(sortByReinvocations().printTopRules(20, namedRules));
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

        public String printTopRules(int count, @NotNull Predicate<RuleReport> filter) {
            StringBuilder sb = new StringBuilder();
            sb.append("Rule                           | Net-Time  | Invocations |   Matches   | Mismatches  |   Time/Invoc.   | Match %  |  Re-Invocs  | Re-Matches  | Re-Mismatch | Re-Invoc %\n");
            sb.append("-------------------------------|-----------|-------------|-------------|-------------|-----------------|----------|-------------|-------------|-------------|-----------\n");
            for (int i = 0; i < Math.min(ruleReports.size(), count); i++) {
                RuleReport rep = ruleReports.get(i);
                if (!filter.apply(rep)) {
                    count++;
                    continue;
                }
                sb.append(String.format("%-30s | %6.0f ms | %,11d | %,11d | %,11d | %,12.0f ns | %6.2f %% | %,11d | %,11d | %,11d | %6.2f %%\n",
                        StringUtils.left(rep.getMatcher().toString() + ": " + rep.getMatcher().getClass().getSimpleName().replace("Matcher", ""), 30),
                        rep.getNanoTime() / 1000000.0,
                        rep.getInvocations(),
                        rep.getMatches(),
                        rep.getMismatches(),
                        rep.getNanoTime() / (double)rep.getInvocations(),
                        rep.getMatchShare() * 100,
                        rep.getReinvocations(),
                        rep.getRematches(),
                        rep.getRemismatches(),
                        rep.getReinvocationShare() * 100
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
                    return doubleCompare(a.getNanoTime() / (double)a.getInvocations(), b.getNanoTime() / (double)b.getInvocations());
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

        public Report sortByMatchShare() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return doubleCompare(a.getMatchShare(), b.getMatchShare());
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

        public Report sortByRematches() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getRematches(), b.getRematches());
                }
            });
            return this;
        }

        public Report sortByReailures() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return intCompare(a.getRemismatches(), b.getRemismatches());
                }
            });
            return this;
        }

        public Report sortByReinvocationShare() {
            Collections.sort(ruleReports, new Comparator<RuleReport>() {
                public int compare(RuleReport a, RuleReport b) {
                    return doubleCompare(a.getReinvocationShare(), b.getReinvocationShare());
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
        private int mismatches;
        private int rematches;
        private int remismatches;
        private long nanoTime;

        public RuleReport(Matcher matcher) {
            this.matcher = matcher;
        }

        public Matcher getMatcher() { return matcher; }
        public int getInvocations() { return matches + mismatches; }
        public int getMatches() { return matches; }
        public int getMismatches() { return mismatches; }
        public double getMatchShare() { return ((double) matches) / getInvocations(); }
        public int getReinvocations() { return rematches + remismatches; }
        public int getRematches() { return rematches; }
        public int getRemismatches() { return remismatches; }
        public double getReinvocationShare() { return ((double) getReinvocations()) / getInvocations(); }
        public long getNanoTime() { return nanoTime; }

        public void update(int matchesDelta, int mismatchesDelta, int rematchesDelta, int remismatchesDelta,
                           long nanoTimeDelta) {
            matches += matchesDelta;
            mismatches += mismatchesDelta;
            rematches += rematchesDelta;
            remismatches += remismatchesDelta;
            nanoTime += nanoTimeDelta;
        }
    }
}