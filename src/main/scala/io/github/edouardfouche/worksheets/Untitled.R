# https://www.rdocumentation.org/packages/dgof/versions/1.2/topics/ks.test

# Linear
x = 1:100
y = x*2
ks.test(x,y)

# Independent
x = runif(100)
y = runif(100)
ks.test(x,y)
