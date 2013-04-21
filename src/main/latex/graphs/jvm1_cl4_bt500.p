set title "Range Query pro max. cluster 4 a stupen BTree 500"
set term postscript eps
set output 'jvm1_cl4_bt500.eps'
set pointsize 1.5
set xlabel 'Maximalni pocet objetu v listu clusteru'
set ylabel 'Cas(ms)'
set logscale x
set key below
plot \
"jvm1_p10_leaf.dat" using 1:10 title 'p10cl4bt500'with linespoints, \
"jvm1_p20_leaf.dat" using 1:10 title 'p20cl4bt500'with linespoints, \
"jvm1_p30_leaf.dat" using 1:10 title 'p30cl4bt500'with linespoints, \
"jvm1_p50_leaf.dat" using 1:10 title 'p50cl4bt500'with linespoints, \
"jvm1_cl4_bt500.dat" using 1:2 title 'p10cl4bt500'with lines, \
"jvm1_cl4_bt500.dat" using 1:3 title 'p20cl4bt500'with lines, \
"jvm1_cl4_bt500.dat" using 1:4 title 'p30cl4bt500'with lines, \
"jvm1_cl4_bt500.dat" using 1:5 title 'p40cl4bt500'with lines
