// A "Compiler" for the Brainfuck language
//============================================================

object BFC {

    // for timing purposes
    def time_needed[T](n: Int, code: => T) = {
        val start = System.nanoTime()
        for (i <- 0 until n) code
        val end = System.nanoTime()
        (end - start)/(n * 1.0e9)
    }

    type Mem = Map[Int, Int]

    import io.Source
    import scala.util._

    //====================== 
    // a couple of functions taken from bf_interpreter.scala
    def load_bff(name: String) : String = Try(Source.fromFile(name)("ISO-8859-1").mkString).getOrElse("")
    def sread(mem: Mem, mp: Int) : Int = mem.getOrElse(mp, 0)
    def write(mem: Mem, mp: Int, v: Int) : Mem = mem + (mp -> v)
    def jumpRight(prog: String, pc: Int, level: Int) : Int = { 
        if (prog.length == pc) pc else {
            prog match {
                case p if p(pc) == '[' => jumpRight(prog, pc+1, level+1)
                case p if p(pc) == ']' => if (level == 0) pc+1 else jumpRight(prog, pc+1, level - 1)
                case _ => jumpRight(prog, pc+1, level)
            }
        }
    }
    def jumpLeft(prog: String, pc: Int, level: Int) : Int = {
        if (pc < 0) pc else {
            prog match {
                case p if p(pc) == ']' => jumpLeft(prog, pc-1, level+1)
                case p if p(pc) == '[' => if (level == 0) pc+1 else jumpLeft(prog, pc-1, level - 1)
                case _ => jumpLeft(prog, pc-1, level)
            }
        }
    }
    //======================

    // generate a jump table for matching brackets in the Brainfuck program
    def jtable(pg: String) : Map[Int, Int] = jtable_helper(pg)

    def jtable_helper(pg: String, pc: Int = 0, ans: Map[Int, Int] = Map()) : Map[Int, Int] = {
        if (pg.length == pc) ans else{
            pg match {
            case p if p(pc) == '[' => jtable_helper(pg, pc+1, (ans + (pc -> jumpRight(pg, pc+1, 0))) )
            case p if p(pc) == ']' => jtable_helper(pg, pc+1, (ans + (pc -> jumpLeft(pg, pc-1, 0))) )
            case _ => jtable_helper(pg, pc+1, ans)
            }
        }
    }

    // compute and execute Brainfuck program using the generated jump table
    def compute2(pg: String, tb: Map[Int, Int], pc: Int, mp: Int, mem: Mem) : Mem = {
        if (pc < 0 || pc >= pg.length) mem else {
            pg match {
                case p if p(pc) == '>' => compute2(pg, tb, pc+1, mp+1, mem)
                case p if p(pc) == '<' => compute2(pg, tb, pc+1, mp-1, mem)
                case p if p(pc) == '+' => compute2(pg, tb, pc+1, mp, write(mem, mp, sread(mem, mp)+1))
                case p if p(pc) == '-' => compute2(pg, tb, pc+1, mp, write(mem, mp, sread(mem, mp)-1))
                case p if p(pc) == '.' => {
                    print(sread(mem, mp).toChar)
                    compute2(pg, tb, pc+1, mp, mem)
                }
                case p if p(pc) == '[' => if (sread(mem, mp) == 0) compute2(pg, tb, tb.get(pc).get, mp, mem) else compute2(pg, tb, pc+1, mp, mem)
                case p if p(pc) == ']' => if (sread(mem, mp) != 0) compute2(pg, tb, tb.get(pc).get, mp, mem) else compute2(pg, tb, pc+1, mp, mem)
                case _ => compute2(pg, tb, pc+1, mp, mem)
            }
        }
    }

    // run Brainfuck program with jump table
    def run2(pg: String, m: Mem = Map()) = compute2(pg, jtable(pg), 0, 0, m)

    // optimize Brainfuck program by removing invalid characters and zero loops
    def optimise(s: String) : String = s.replaceAll("""[^<>+\-.\[\]]""", "").replaceAll("""\[-\]""", "0")

    def compute3(pg: String, tb: Map[Int, Int], pc: Int, mp: Int, mem: Mem) : Mem = {
        if (pc < 0 || pc >= pg.length) mem else {
            pg match {
                case p if p(pc) == '>' => compute3(pg, tb, pc+1, mp+1, mem)
                case p if p(pc) == '<' => compute3(pg, tb, pc+1, mp-1, mem)
                case p if p(pc) == '+' => compute3(pg, tb, pc+1, mp, write(mem, mp, sread(mem, mp)+1))
                case p if p(pc) == '-' => compute3(pg, tb, pc+1, mp, write(mem, mp, sread(mem, mp)-1))
                case p if p(pc) == '0' => compute3(pg, tb, pc+1, mp, write(mem, mp, 0))
                case p if p(pc) == '.' => {
                    print(sread(mem, mp).toChar)
                    compute3(pg, tb, pc+1, mp, mem)
                }
                case p if p(pc) == '[' => if (sread(mem, mp) == 0) compute3(pg, tb, tb.get(pc).get, mp, mem) else compute3(pg, tb, pc+1, mp, mem)
                case p if p(pc) == ']' => if (sread(mem, mp) != 0) compute3(pg, tb, tb.get(pc).get, mp, mem) else compute3(pg, tb, pc+1, mp, mem)
                
                case _ => compute3(pg, tb, pc+1, mp, mem)
            }
        }
    }

    def run3(pg: String, m: Mem = Map()) = compute3(optimise(pg), jtable(optimise(pg)), 0, 0, m)

    // Function to combine adjacent identical commands in the Brainfuck program
    def combine(s: String) : String = combine_helper(s, c=s(0))
    val good_stuff = List('[', ']', '0', '.')

    def combine_helper(string: String, pc: Int = 0, counter: Int = 0, c: Char) : String = {
        if (pc == string.length) {
            if (good_stuff.contains(string(pc-1))) string else command_gen(string, pc, counter, c)
        } else {
            string match {
                case s if (good_stuff.contains(s(pc)) && counter == 0 )=> combine_helper(string, pc+1, 0, if (pc+1 != s.length) s(pc+1) else s(pc))
                case s if s(pc) != c => {
                    val command = command_gen(string, pc, counter, c)
                    combine_helper(command + s.slice(pc, s.length), command.length, 0, s(pc))
                }
                case _ => combine_helper(string, pc+1, counter+1, c)
            }
        }
    }

    // generate command based on the count of repeated commands ( maybe will rewrite in the future idk)
    def command_gen(s: String, pc: Int, counter: Int, c: Char) : String = {
        if (counter <= 26) {
            s.slice(0, pc-counter) + c.toString + (64 + counter).toChar
        }else{
            val new_ptr = 26 + pc - counter
            s.slice(0, pc-counter) + c.toString + "Z" + command_gen(s, pc - new_ptr, counter - 26, c)
        }
    }

    def charToInt(char: Char) : Int = char.toInt - 64

    def compute4(pg: String, tb: Map[Int, Int], pc: Int, mp: Int, mem: Mem) : Mem = {
        if (pc < 0 || pc >= pg.length) mem else {
            pg match {
                case p if p(pc) == '>' => compute4(pg, tb, pc+2, mp+charToInt(p(pc+1)), mem)
                case p if p(pc) == '<' => compute4(pg, tb, pc+2, mp-charToInt(p(pc+1)), mem)
                case p if p(pc) == '+' => compute4(pg, tb, pc+2, mp, write(mem, mp, sread(mem, mp)+charToInt(p(pc+1))))
                case p if p(pc) == '-' => compute4(pg, tb, pc+2, mp, write(mem, mp, sread(mem, mp)-charToInt(p(pc+1))))
                // should be unchanged
                case p if p(pc) == '0' => compute4(pg, tb, pc+1, mp, write(mem, mp, 0))
                case p if p(pc) == '.' => {
                    print(sread(mem, mp).toChar)
                    compute4(pg, tb, pc+1, mp, mem)
                }
                case p if p(pc) == '[' => if (sread(mem, mp) == 0) compute4(pg, tb, tb.get(pc).get, mp, mem) else compute4(pg, tb, pc+1, mp, mem)
                case p if p(pc) == ']' => if (sread(mem, mp) != 0) compute4(pg, tb, tb.get(pc).get, mp, mem) else compute4(pg, tb, pc+1, mp, mem)
                
                case _ => compute4(pg, tb, pc+1, mp, mem)
            }
        }
    }

    // run modified Brainfuck program with character-based commands (should call first optimise and then combine on the input string)
    def run4(pg: String, m: Mem = Map()) = {
        val new_pg = combine(optimise(pg))
        compute4(new_pg, jtable(new_pg), 0, 0, m)
    }
}
