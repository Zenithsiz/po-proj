#!/bin/env bash

let total=0
let correct=0

# Build
./build.sh

for x in tests/*.in; do
	echo -n "$x: "
	
	if [ -e ${x%.in}.import ]; then
		java -ea -cp :po-uilib.jar:. -Dimport=${x%.in}.import -Din=$x -Dout=${x%.in}.outhyp ggc.app.App || break
	else
		java -ea -cp po-uilib.jar:. -Din=$x -Dout=${x%.in}.outhyp ggc.app.App || break
	fi

	diff -cwB ${x%.in}.out ${x%.in}.outhyp > ${x%.in}.diff
	if [ -s ${x%.in}.diff ]; then
		echo "Fail"
		failures=$failures"Fail: $x: See file ${x%.in}.diff\n"
	else
		let correct++
		echo "Ok"
		rm -f ${x%.in}.diff ${x%.in}.outhyp
	fi
	let total++
done

# Remove any remaining `app*.dat` files and `.ggc` files
rm -f app*.dat
rm -f *.ggc

let res=100*$correct/$total
echo ""
echo "Total Tests = " $total
echo "Passed = " $res"%"
printf "$failures"
echo "Done."

