// An Interpreter for the Brainfuck language
//==============================================

object BFI {
    import io.Source
    import scala.util._

    // representation of BF memory 
    type Mem = Map[Int, Int]

    // Load Brainfuck program from file
    def load_bff(name: String) : String = Try(Source.fromFile(name)("ISO-8859-1").mkString).getOrElse("")

    // Read and write operations on memory
    def sread(mem: Mem, mp: Int) : Int = mem.getOrElse(mp, 0)

    def write(mem: Mem, mp: Int, v: Int) : Mem = mem + (mp -> v)

    // Find matching brackets in Brainfuck program
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

    // Interpret and execute Brainfuck program
    def compute(prog: String, pc: Int, mp: Int, mem: Mem) : Mem = {
        if (pc < 0 || pc >= prog.length) mem else {
            prog match {
                case p if p(pc) == '>' => compute(prog, pc+1, mp+1, mem)
                case p if p(pc) == '<' => compute(prog, pc+1, mp-1, mem)
                case p if p(pc) == '+' => compute(prog, pc+1, mp, write(mem, mp, sread(mem, mp)+1))
                case p if p(pc) == '-' => compute(prog, pc+1, mp, write(mem, mp, sread(mem, mp)-1))
                case p if p(pc) == '.' => {
                    print(sread(mem, mp).toChar)
                    compute(prog, pc+1, mp, mem)
                }
                case p if p(pc) == '[' => if (sread(mem, mp) == 0) compute(prog, jumpRight(prog, pc+1, 0), mp, mem) else compute(prog, pc+1, mp, mem)
                case p if p(pc) == ']' => if (sread(mem, mp) != 0) compute(prog, jumpLeft(prog, pc-1, 0), mp, mem) else compute(prog, pc+1, mp, mem)
                case _ => compute(prog, pc+1, mp, mem)
            }
        }
    }

    def run(prog: String, m: Mem = Map()) = compute(prog, 0, 0, m)

    // Generate Brainfuck program to print characters
    def generate(msg: List[Char]) : String = msg.map(c => ("+" * c.toInt) ++ ".[-]").mkString
}