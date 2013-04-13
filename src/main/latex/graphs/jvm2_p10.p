set title "Range Query pro p = 10"
set term postscript eps
set output 'jvm2_p10.eps'
set pointsize 1.5
set xlabel 'stupen B-Tree'
set ylabel 'Cas(ms)'
set logscale x
set key below
plot \
"jvm2_p10.dat" using 1:2 title 'p10cl2l10'with linespoints, \
"jvm2_p10.dat" using 1:3 title 'p10cl2l20'with linespoints, \
"jvm2_p10.dat" using 1:4 title 'p10cl2l50'with linespoints, \
"jvm2_p10.dat" using 1:5 title 'p10cl2l100'with linespoints, \
"jvm2_p10.dat" using 1:6 title 'p10cl2l200'with linespoints, \
"jvm2_p10.dat" using 1:7 title 'p10cl2l300'with linespoints, \
"jvm2_p10.dat" using 1:8 title 'p10cl2l500'with linespoints, \
"jvm2_p10.dat" using 1:9 title 'p10cl3l10'with linespoints, \
"jvm2_p10.dat" using 1:10 title 'p10cl3l20'with linespoints, \
"jvm2_p10.dat" using 1:11 title 'p10cl3l50'with linespoints, \
"jvm2_p10.dat" using 1:12 title 'p10cl3l100'with linespoints, \
"jvm2_p10.dat" using 1:13 title 'p10cl3l200'with linespoints, \
"jvm2_p10.dat" using 1:14 title 'p10cl3l300'with linespoints, \
"jvm2_p10.dat" using 1:15 title 'p10cl3l500'with linespoints, \
"jvm2_p10.dat" using 1:16 title 'p10cl4l10'with linespoints, \
"jvm2_p10.dat" using 1:17 title 'p10cl4l20'with linespoints, \
"jvm2_p10.dat" using 1:18 title 'p10cl4l50'with linespoints, \
"jvm2_p10.dat" using 1:19 title 'p10cl4l100'with linespoints, \
"jvm2_p10.dat" using 1:20 title 'p10cl4l200'with linespoints, \
"jvm2_p10.dat" using 1:21 title 'p10cl4l300'with linespoints, \
"jvm2_p10.dat" using 1:22 title 'p10cl4l500'with linespoints, \
"jvm2_p10.dat" using 1:23 title 'p10cl2'with linespoints, \
"jvm2_p10.dat" using 1:24 title 'p10cl3'with linespoints, \
"jvm2_p10.dat" using 1:25 title 'p10cl4'with linespoints
