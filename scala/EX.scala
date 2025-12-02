package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class Execute extends MultiIOModule {

  val io = IO(
    new Bundle {
        // UInts ????? idfk

      val in = Input(new IDBarrierContent)
      val out = Output(new EXBarrierContent)

      val WBDestReg = Input(UInt(32.W))
      val WBResult = Input(UInt(32.W))

      val WBLateDestReg = Input(UInt(32.W))
      val WBLateResult = Input(UInt(32.W))

      val WBLateLateDestReg = Input(UInt(32.W))
      val WBLateLateResult = Input(UInt(32.W))

      val MEMDestReg = Input(UInt(32.W))
      val MEMResult = Input(UInt(32.W))
      val MEMLoad = Input(Bool())

      
      val freezeOut = Output(Bool())
      
    })

    

    val decoder   = Module(new Decoder).io
    decoder.instruction := io.in.instruction

    io.out.PC := io.in.PC
    io.out.controlSignals := io.in.controlSignals
    io.out.instruction := io.in.instruction

    val ALU = Module(new ALU)
    val Forward = Module(new Forward)

    Forward.io.EXR1 := decoder.instruction.registerRs1
    Forward.io.EXR2 := decoder.instruction.registerRs2

    Forward.io.EXR1Value := io.in.r1
    Forward.io.EXR2Value := io.in.r2

    Forward.io.WBDestReg := io.WBDestReg
    Forward.io.WBResult := io.WBResult

    Forward.io.WBLateDestReg := io.WBLateDestReg
    Forward.io.WBLateResult := io.WBLateResult

    Forward.io.WBLateLateDestReg := io.WBLateLateDestReg
    Forward.io.WBLateLateResult := io.WBLateLateResult

    Forward.io.MEMDestReg := io.MEMDestReg
    Forward.io.MEMResult := io.MEMResult
    Forward.io.MEMLoad := io.MEMLoad

    io.freezeOut := Forward.io.ForwardLoadMem


    
    when(decoder.op2Select === Op2Select.imm || decoder.instruction.registerRs2 === 0.U) {
        ALU.io.input2 := io.in.imm
        
    }.otherwise {
        //ALU.io.input2 := io.in.r2
        ALU.io.input2 := Forward.io.R2Output
    }

    io.out.r2 := Forward.io.R2Output

    when(decoder.instruction.registerRs1 === 0.U) {
      ALU.io.input1 := io.in.r1
    }.otherwise{
       ALU.io.input1 := Forward.io.R1Output
    }
    
    
    //ALU.io.input1 := io.in.r1
   
    ALU.io.ALUop := decoder.ALUop

    io.out.ALUResult := ALU.io.ALUResult

    io.out.jump := false.B
    io.out.targetPC := 0.U

    when (decoder.controlSignals.jump) {
      io.out.targetPC := (io.in.PC + io.in.imm)
      io.out.ALUResult := io.in.PC + 4.U
      io.out.jump:= true.B
      when (lookup.JALR === io.in.instruction.asUInt) {
        io.out.targetPC := (Forward.io.R1Output + io.in.imm) & "xfffffffe".U
        io.out.ALUResult := io.in.PC + 4.U
      }
    }.elsewhen (decoder.controlSignals.branch) {
      val branch_taken = WireInit(false.B)

      when (decoder.branchType === branchType.beq) {branch_taken := Forward.io.R1Output  === Forward.io.R2Output}
      .elsewhen(decoder.branchType === branchType.gte) { branch_taken := Forward.io.R1Output.asSInt >=  Forward.io.R2Output.asSInt }
      .elsewhen(decoder.branchType === branchType.gteu) { branch_taken := Forward.io.R1Output  >=  Forward.io.R2Output  }
      .elsewhen(decoder.branchType === branchType.lt) { branch_taken := Forward.io.R1Output.asSInt <   Forward.io.R2Output.asSInt }
      .elsewhen(decoder.branchType === branchType.ltu) { branch_taken := Forward.io.R1Output  <   Forward.io.R2Output  }
      .elsewhen(decoder.branchType === branchType.neq) { branch_taken := Forward.io.R1Output  =/= Forward.io.R2Output  }
      
      when (branch_taken) {
        io.out.targetPC := (io.in.PC + io.in.imm)
        io.out.ALUResult := io.in.PC + 4.U
        io.out.jump:= true.B
      }


    }

}
