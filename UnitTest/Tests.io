## input1
alarm_net.xml
B-E|
END_INPUT
yes
END_OUTPUT
## input2
alarm_net.xml
B-E|J=T
END_INPUT
no
END_OUTPUT
## input3
alarm_net.xml
P(B=T|J=T,M=T) A-E
END_INPUT
0.28417,7,16
END_OUTPUT
## input4
alarm_net.xml
P(B=T|J=T,M=T) E-A
END_INPUT
0.28417,7,16
END_OUTPUT
## input5
alarm_net.xml
P(J=T|B=T) A-E-M
END_INPUT
0.84902,7,12
END_OUTPUT
## input6
alarm_net.xml
P(J=T|B=T) M-E-A
END_INPUT
0.84902,5,8
END_OUTPUT
## input7
big_net.xml
B0-C2|A2=T,A3=T
END_INPUT
yes
END_OUTPUT
## input8
big_net.xml
A1-D1|C3=T,B2=F,B3=F
END_INPUT
no
END_OUTPUT
## input9
big_net.xml
P(B0=v3|C3=T,B2=F,C2=v3) A2-D1-B3-C1-A1-B1-A3
END_INPUT
0.42307,10,21
END_OUTPUT
## input10
net3.xml
S-N|F=nice
END_INPUT
yes
END_OUTPUT
## input11
net3.xml
P(M=Y|F=nice) N-S
END_INPUT
0.75680,11,18
END_OUTPUT
## input12
net3.xml
P(M=Y|F=nice,S=bad) N
END_INPUT
0.38000,3,4
END_OUTPUT
## input13
net3.xml
P(M=N|S=good) F-N
END_INPUT
0.25718,11,18
END_OUTPUT
## input14
net3.xml
P(F=nice|N=T) M-S
END_INPUT
0.70588,2,3
END_OUTPUT
## input15
net3.xml
P(F=boring|S=ok) M-N
END_INPUT
0.30000,0,0
END_OUTPUT
## input16
net4.xml
D-L|S=T
END_INPUT
no
END_OUTPUT
## input17
net4.xml
P(I=T|G=fine) D-S-L
END_INPUT
0.70155,3,6
END_OUTPUT
## input18
net4.xml
P(L=F|D=T) I-G-S
END_INPUT
0.41970,8,12
END_OUTPUT
## input19
net4.xml
P(G=low|D=F) I-L-S
END_INPUT
0.78000,5,6
END_OUTPUT
## input20
net4.xml
P(G=low|D=F,I=T) L-S
END_INPUT
0.90000,0,0
END_OUTPUT
## input21
net4.xml
P(S=F|G=high,L=F) D-I
END_INPUT
0.44950,5,10
END_OUTPUT
## input22
net4.xml
P(I=T|L=T) D-G-S
END_INPUT
0.56532,11,20
END_OUTPUT
## input23
net4.xml
P(G=fine|S=T) L-D-I
END_INPUT
0.27183,11,20
END_OUTPUT
## input24
net5.xml
B-G|F=always
END_INPUT
no
END_OUTPUT
## input25
net5.xml
P(H=T|G=high) B-F
END_INPUT
0.20000,0,0
END_OUTPUT
## input26
net5.xml
P(H=T|F=never,B=F) G
END_INPUT
0.27000,5,6
END_OUTPUT
## input27
net5.xml
P(G=medium|B=T) H-F
END_INPUT
0.26000,8,9
END_OUTPUT
## input28
net5.xml
P(F=always|B=T,H=T) G
END_INPUT
0.47222,8,12
END_OUTPUT
## input29
net5.xml
P(G=medium|H=T) B-F
END_INPUT
0.10214,17,30
END_OUTPUT
## input30
net5.xml
P(G=low|F=always) B-H
END_INPUT
0.70000,5,6
END_OUTPUT
## input31
net6.xml
A-C|B=noset
END_INPUT
no
END_OUTPUT
## input32
net6.xml
A-B|
END_INPUT
yes
END_OUTPUT
## input33
net6.xml
P(C=run|B=set,A=T)
END_INPUT
0.05000,0,0
END_OUTPUT
## input34
net6.xml
P(A=T|C=run) B
END_INPUT
0.07429,5,8
END_OUTPUT
## input35
net6.xml
P(A=F|C=stay) B
END_INPUT
0.82111,5,8
END_OUTPUT
## input36
net7.xml
F-G|C=T,E=two,H=yes
END_INPUT
no
END_OUTPUT
## input37
net7.xml
P(A=T|E=two,F=two) B-C-D-G-I-H
END_INPUT
0.11906,15,34
END_OUTPUT
## input38
net7.xml
P(A=F|B=T,C=F,D=T) E-H-F-I-G
END_INPUT
0.96183,1,6
END_OUTPUT
## input39
net7.xml
P(G=one|B=T,I=ken) H-E-A-C-D-F
END_INPUT
0.71608,21,44
END_OUTPUT
## input40
net7.xml
P(D=T|E=one) H-I-A-C-G-F-B
END_INPUT
0.39705,7,16
END_OUTPUT
## input41
net8.xml
A-B|
END_INPUT
yes
END_OUTPUT
## input42
net8.xml
G-F|D=T,E=F,A=F
END_INPUT
yes
END_OUTPUT
## input43
net8.xml
P(B=T|D=T) A-C-E-F-G
END_INPUT
0.29344,3,6
END_OUTPUT
## input44
net8.xml
P(D=F|A=T,E=F) B-C-F-G
END_INPUT
0.47264,3,6
END_OUTPUT
## input45
net8.xml
P(A=T|C=T,F=T,E=F) D-B-G
END_INPUT
1.00000,7,18
END_OUTPUT
## input46
net9.xml
S-R|
END_INPUT
no
END_OUTPUT
## input47
net9.xml
P(S=T|C=F) R-W
END_INPUT
0.50000,0,0
END_OUTPUT
## input48
net9.xml
P(S=T|C=F,W=F) R
END_INPUT
0.09091,3,6
END_OUTPUT
## input49
net9.xml
P(S=T|W=T) R-C
END_INPUT
0.42976,7,16
END_OUTPUT
## input50
net9.xml
P(R=F|S=T,W=F) C
END_INPUT
0.95890,3,8
END_OUTPUT
## input51
net10.xml
R-S|
END_INPUT
no
END_OUTPUT
## input52
net10.xml
P(R=T|S=T) G
END_INPUT
0.00621,1,2
END_OUTPUT
## input53
net10.xml
P(R=F|S=T) G
END_INPUT
0.99379,1,2
END_OUTPUT
## input54
net10.xml
P(S=T|G=T) R
END_INPUT
0.64673,3,8
END_OUTPUT
## input55
net10.xml
P(G=F|R=F) S
END_INPUT
0.64000,3,4
END_OUTPUT
