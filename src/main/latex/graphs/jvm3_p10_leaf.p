set title "Range Query pro p = 10"
set term postscript eps
set output 'jvm3_p10_leaf.eps'
set pointsize 1.5
set xlabel 'Maximalni pocet objetu v listu clusteru'
set ylabel 'Cas(ms)'
set logscale x
set key below
plot \
"jvm3_p10_leaf.dat" using 1:2 title 'p10cl2bt50'with linespoints, \
"jvm3_p10_leaf.dat" using 1:3 title 'p10cl2bt100'with linespoints, \
"jvm3_p10_leaf.dat" using 1:4 title 'p10cl2bt500'with linespoints, \
"jvm3_p10_leaf.dat" using 1:5 title 'p10cl3bt50'with linespoints, \
"jvm3_p10_leaf.dat" using 1:6 title 'p10cl3bt100'with linespoints, \
"jvm3_p10_leaf.dat" using 1:7 title 'p10cl3bt500'with linespoints, \
"jvm3_p10_leaf.dat" using 1:8 title 'p10cl4bt50'with linespoints, \
"jvm3_p10_leaf.dat" using 1:9 title 'p10cl4bt100'with linespoints, \
"jvm3_p10_leaf.dat" using 1:10 title 'p10cl4bt500'with linespoints
