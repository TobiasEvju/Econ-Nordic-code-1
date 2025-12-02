package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util._

class ALU extends MultiIOModule {
  val io = IO(
    new Bundle {
        //Uint ????? idfk
      val ALUop = Input(UInt(32.W))
      val input1 = Input(UInt(32.W))
      val input2 = Input(UInt(32.W))

      val ALUResult = Output(UInt(32.W))
    })

    val ALUopMap = Array(
  ALUOps.ADD    -> (io.input1 + io.input2),
  ALUOps.SUB    -> (io.input1 - io.input2),
  ALUOps.AND    -> (io.input1 & io.input2),
  ALUOps.OR    -> (io.input1 | io.input2),
  ALUOps.XOR    -> (io.input1 ^ io.input2),
  ALUOps.SLT    -> (io.input1.asSInt < io.input2.asSInt),
  ALUOps.SLTU    -> (io.input1 < io.input2),
  ALUOps.SRA    -> (io.input1.asSInt >> io.input2(4, 0)).asUInt,
  ALUOps.SRL    -> (io.input1 >> io.input2(4, 0)),
  ALUOps.SLL    -> (io.input1 << io.input2(4, 0)),
  )

  io.ALUResult := MuxLookup(io.ALUop, 0.U(32.W), ALUopMap)
}
