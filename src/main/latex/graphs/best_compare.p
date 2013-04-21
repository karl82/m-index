set term postscript eps
set output 'best_compare.eps'
set pointsize 1.5
set xlabel 'stupen B-Tree'
set ylabel 'Cas(ms)'
set logscale x
set key below
plot \
"jvm1_p10.dat" using 1:16 title 'JVM1 p10cl4l10'with linespoints, \
"jvm2_p10.dat" using 1:16 title 'JVM2 p10cl4l10'with linespoints, \
"jvm3_p10.dat" using 1:16 title 'JVM3 p10cl4l10'with linespoints, \
"jvm4_p10.dat" using 1:17 title 'JVM4 p10cl4l20'with linespoints, \
"jvm4_p50.dat" using 1:25 title 'JVM4 p50cl4'with linespoints
