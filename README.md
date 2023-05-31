# Compiler tests
```scala
jtable("""+++++[->++++++++++<]>--<+++[->>++++++++++<<]>>++<<----------[+>.>.<+<]""") =>  Map(69 -> 61, 5 -> 20, 60 -> 70, 27 -> 44, 43 -> 28, 19 -> 6)

time_needed(1, run2(load_bff("benchmark.bf")))
time_needed(1, run2(load_bff("sierpinski.bf")))

optimise(load_bff("benchmark.bf"))          // should have inserted 0's
optimise(load_bff("mandelbrot.bf")).length  // => 11203

time_needed(1, run3(load_bff("benchmark.bf")))

combine(load_bff("benchmark.bf"))
combine(optimise(load_bff("benchmark.bf"))) // => """>A+B[<A+M>A-A]<A[[....."""

testcases (they should now run much faster)
time_needed(1, run4(load_bff("benchmark.bf")))
time_needed(1, run4(load_bff("sierpinski.bf"))) 
time_needed(1, run4(load_bff("mandelbrot.bf")))
```

# Interpreter tests
```scala
// testcases 
jumpRight("""--[..+>--],>,++""", 3, 0)         // => 10
jumpLeft("""--[..+>--],>,++""", 8, 0)          // => 3
jumpRight("""--[..[+>]--],>,++""", 3, 0)       // => 12
jumpRight("""--[..[[-]+>[.]]--],>,++""", 3, 0) // => 18
jumpRight("""--[..[[-]+>[.]]--,>,++""", 3, 0)  // => 22 (outside)
jumpLeft("""[******]***""", 7, 0)              // => -1 (outside)
```

## Some sample bf-programs collected from the Internet
- some contrived (small) programs

```scala
// clears the 0-cell
run("[-]", Map(0 -> 100))    // Map will be 0 -> 0

// moves content of the 0-cell to 1-cell
run("[->+<]", Map(0 -> 10))  // Map will be 0 -> 0, 1 -> 10

// copies content of the 0-cell to 2-cell and 4-cell
run("[>>+>>+<<<<-]", Map(0 -> 42))    // Map(0 -> 0, 2 -> 42, 4 -> 42)

// prints out numbers 0 to 9
run("""+++++[->++++++++++<]>--<+++[->>++++++++++<<]>>++<<----------[+>.>.<+<]""")
```

- some more "useful" programs
```scala
// hello world program 1
run("""++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.""")

// hello world program 2
run("""++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.""")

// hello world program 3
run("""+++++++++[>++++++++>+++++++++++>+++++<<<-]>.>++.+++++++..+++.>-.------------.<++++++++.--------.+++.------.--------.>+.""")

// draws the Sierpinski triangle
run(load_bff("sierpinski.bf"))
```

 - bigger programs
```scala
// fibonacci numbers below 100
run("""+++++++++++
     >+>>>>++++++++++++++++++++++++++++++++++++++++++++
     >++++++++++++++++++++++++++++++++<<<<<<[>[>>>>>>+>
     +<<<<<<<-]>>>>>>>[<<<<<<<+>>>>>>>-]<[>++++++++++[-
     <-[>>+>+<<<-]>>>[<<<+>>>-]+<[>[-]<[-]]>[<<[>>>+<<<
     -]>>[-]]<<]>>>[>>+>+<<<-]>>>[<<<+>>>-]+<[>[-]<[-]]
     >[<<+>>[-]]<<<<<<<]>>>>>[+++++++++++++++++++++++++
     +++++++++++++++++++++++.[-]]++++++++++<[->-<]>++++
     ++++++++++++++++++++++++++++++++++++++++++++.[-]<<
     <<<<<<<<<<[>>>+>+<<<<-]>>>>[<<<<+>>>>-]<-[>>.>.<<<
     [-]]<<[>>+>+<<<-]>>>[<<<+>>>-]<<[<+>-]>[<+>-]<<<-]""")

// outputs the square numbers up to 10000
run("""++++[>+++++<-]>[<+++++>-]+<+[>[>+>+<<-]++>>[<<+>>-]>>>[-]++>[-]+
      >>>+[[-]++++++>>>]<<<[[<++++++++<++>>-]+<.<[>----<-]<]
      <<[>>>>>[>>>[-]+++++++++<[>-<-]+++++++++>[-[<->-]+[<<<]]<[>+<-]>]<<-]<<-]""")


// calculates 2 to the power of 6 (example from a C-to-BF compiler at https://github.com/elikaski/BF-it)
run(""">>[-]>[-]++>[-]++++++><<<>>>>[-]+><>[-]<<[-]>[>+<<+>-]>[<+>-]
      <><[-]>[-]<<<[>>+>+<<<-]>>>[<<<+>>>-][-]><<>>[-]>[-]<<<[>>[-]
      <[>+>+<<-]>[<+>-]+>[[-]<-<->>]<<<-]>>[<<+>>-]<<[[-]>[-]<<[>+>
      +<<-]>>[<<+>>-][-]>[-]<<<<<[>>>>+>+<<<<<-]>>>>>[<<<<<+>>>>>-]
      <<>>[-]>[-]<<<[>>>+<<<-]>>>[<<[<+>>+<-]>[<+>-]>-]<<<>[-]<<[-]
      >[>+<<+>-]>[<+>-]<><[-]>[-]<<<[>>+>+<<<-]>>>-[<<<+>>>-]<[-]>[-]
      <<<[>>+>+<<<-]>>>[<<<+>>>-][-]><<>>[-]>[-]<<<[>>[-]<[>+>+<<-]>
      [<+>-]+>[[-]<-<->>]<<<-]>>[<<+>>-]<<][-]>[-]<<[>+>+<<-]>>[<<+>
      >-]<<<<<[-]>>>>[<<<<+>>>>-]<<<<><>[-]<<[-]>[>+<<+>-]>[<+>-]<>
      <[-]>[-]>[-]<<<[>>+>+<<<-]>>>[<<<+>>>-]<<>>[-]>[-]>[-]>[-]>[-]>
      [-]>[-]>[-]>[-]>[-]<<<<<<<<<<>>++++++++++<<[->+>-[>+>>]>[+[-<+
      >]>+>>]<<<<<<]>>[-]>>>++++++++++<[->-[>+>>]>[+[-<+>]>+>>]<<<<<
      ]>[-]>>[>++++++[-<++++++++>]<.<<+>+>[-]]<[<[->-<]++++++[->++++
      ++++<]>.[-]]<<++++++[-<++++++++>]<.[-]<<[-<+>]<<><<<""")


// a Mandelbrot set generator in brainfuck written by Erik Bosman (http://esoteric.sange.fi/brainfuck/utils/mandelbrot/)
run(load_bff("mandelbrot.bf"))

// a benchmark program (counts down from 'Z' to 'A')
run(load_bff("benchmark.bf"))

// calculates the Collatz series for numbers from 1 to 30
run(load_bff("collatz.bf"))
```
