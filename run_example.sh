#!/bin/sh
if [ $# -ne 1 ]
then
	echo "$0 : Invalid arguments!"
	echo "Example usage: run_example org.parboiled.examples.calculator.Main"
	exit 1
fi

echo "Running 'ant compile'..."

ant compile > /dev/null 2>&1

if [ $? -ne 0 ]
then
	echo "Compile failed, run 'ant compile' and check the error message(s)!"
	exit 1
fi

echo "Starting '$1'..."

java -cp "build/classes:lib/cglib/cglib-nodep-2.2.jar" $1