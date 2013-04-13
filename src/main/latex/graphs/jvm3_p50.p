set title "Range Query pro p = 50"
set term postscript eps
set output 'jvm3_p50.eps'
set pointsize 1.5
set xlabel 'stupen B-Tree'
set ylabel 'Cas(ms)'
set logscale x
set key below
plot \
"jvm3_p50.dat" using 1:2 title 'p50cl2l10'with linespoints, \
"jvm3_p50.dat" using 1:3 title 'p50cl2l20'with linespoints, \
"jvm3_p50.dat" using 1:4 title 'p50cl2l50'with linespoints, \
"jvm3_p50.dat" using 1:5 title 'p50cl2l100'with linespoints, \
"jvm3_p50.dat" using 1:6 title 'p50cl2l200'with linespoints, \
"jvm3_p50.dat" using 1:7 title 'p50cl2l300'with linespoints, \
"jvm3_p50.dat" using 1:8 title 'p50cl2l500'with linespoints, \
"jvm3_p50.dat" using 1:9 title 'p50cl3l10'with linespoints, \
"jvm3_p50.dat" using 1:10 title 'p50cl3l20'with linespoints, \
"jvm3_p50.dat" using 1:11 title 'p50cl3l50'with linespoints, \
"jvm3_p50.dat" using 1:12 title 'p50cl3l100'with linespoints, \
"jvm3_p50.dat" using 1:13 title 'p50cl3l200'with linespoints, \
"jvm3_p50.dat" using 1:14 title 'p50cl3l300'with linespoints, \
"jvm3_p50.dat" using 1:15 title 'p50cl3l500'with linespoints, \
"jvm3_p50.dat" using 1:16 title 'p50cl4l10'with linespoints, \
"jvm3_p50.dat" using 1:17 title 'p50cl4l20'with linespoints, \
"jvm3_p50.dat" using 1:18 title 'p50cl4l50'with linespoints, \
"jvm3_p50.dat" using 1:19 title 'p50cl4l100'with linespoints, \
"jvm3_p50.dat" using 1:20 title 'p50cl4l200'with linespoints, \
"jvm3_p50.dat" using 1:21 title 'p50cl4l300'with linespoints, \
"jvm3_p50.dat" using 1:22 title 'p50cl4l500'with linespoints, \
"jvm3_p50.dat" using 1:23 title 'p50cl2'with linespoints, \
"jvm3_p50.dat" using 1:24 title 'p50cl3'with linespoints, \
"jvm3_p50.dat" using 1:25 title 'p50cl4'with linespoints
