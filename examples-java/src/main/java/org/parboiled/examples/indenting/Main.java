package org.parboiled.examples.indenting;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

import org.parboiled.Parboiled;
import org.parboiled.buffers.IndentDedentInputBuffer;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class Main {

    public static void main(String[] args) {
        SimpleIndent parser = Parboiled.createParser(SimpleIndent.class);
        String input = "NodeA \n\tNodeB\n\tNodeC \n\t\tNodeD \nNodeE";

        ParsingResult<?> result = new ReportingParseRunner(parser.Parent())
                .run(new IndentDedentInputBuffer(input.toCharArray(), 2, ";", true, true));

        if (!result.parseErrors.isEmpty()) {
            System.out.println(ErrorUtils.printParseError(result.parseErrors
                    .get(0)));
        } else {
            System.out.println("NodeTree: " + printNodeTree(result) + '\n');
            Object value = result.parseTreeRoot.getValue();
            System.out.println(value.toString());
        }

    }
}