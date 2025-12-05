package computerdesign.theory;

/**
 * DigitalLogic - Understanding the building blocks of computer hardware.
 * 
 * Covers learning goals: T3.2 (Combinational Logic), T4.2 (Sequential Logic)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * COMBINATIONAL vs SEQUENTIAL LOGIC
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * COMBINATIONAL LOGIC:
 *   - Output depends ONLY on current inputs
 *   - No memory, no state
 *   - Examples: AND gate, ALU, multiplexer, decoder
 *   - Used in: Datapath computation, address decoding
 * 
 * SEQUENTIAL LOGIC:
 *   - Output depends on current inputs AND previous state
 *   - Has memory, maintains state
 *   - Examples: Flip-flops, registers, counters, FSMs
 *   - Used in: Registers, pipeline registers, control units
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * BASIC LOGIC GATES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * NOT (Inverter):          AND:                    OR:
 *   ┌───┐                   A ──┬──┐               A ──┬──╲
 *   │ ▷○├── Y = Ā           B ──┴──│D──── Y        B ──┴──╱D──── Y
 *   └───┘                                          
 *                         Y = A · B               Y = A + B
 * 
 * A│Y                    A B│Y                   A B│Y
 * ─┼─                    ───┼─                   ───┼─
 * 0│1                    0 0│0                   0 0│0
 * 1│0                    0 1│0                   0 1│1
 *                        1 0│0                   1 0│1
 *                        1 1│1                   1 1│1
 * 
 * NAND (NOT-AND):          NOR (NOT-OR):           XOR:
 *   Y = ¬(A · B)            Y = ¬(A + B)            Y = A ⊕ B
 * 
 * A B│Y                   A B│Y                   A B│Y
 * ───┼─                   ───┼─                   ───┼─
 * 0 0│1                   0 0│1                   0 0│0
 * 0 1│1                   0 1│0                   0 1│1
 * 1 0│1                   1 0│0                   1 0│1
 * 1 1│0                   1 1│0                   1 1│0
 * 
 * UNIVERSAL GATES: NAND and NOR are "universal" - any logic function can be
 * built using only NAND gates or only NOR gates!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * BOOLEAN ALGEBRA & SUM OF PRODUCTS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Any truth table can be expressed in SUM OF PRODUCTS (SOP) form:
 * 
 * Example: Majority function (output 1 if at least 2 inputs are 1)
 * 
 *   A B C │ Y              Find rows where Y=1:
 *   ──────┼──              
 *   0 0 0 │ 0              Y = ĀBC + AB̄C + ABC̄ + ABC
 *   0 0 1 │ 0                   │      │      │      │
 *   0 1 0 │ 0               (011) (101) (110) (111)
 *   0 1 1 │ 1  ←
 *   1 0 0 │ 0              Simplified: Y = AB + AC + BC
 *   1 0 1 │ 1  ←
 *   1 1 0 │ 1  ←
 *   1 1 1 │ 1  ←
 * 
 * SOP PROCEDURE:
 *   1. For each row where output is 1, write a product (AND) term
 *   2. In the product: use variable if 1, complement if 0
 *   3. OR all product terms together
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * MULTIPLEXER (MUX)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Selects one of N inputs based on select lines.
 * 
 *   2-to-1 MUX:                      4-to-1 MUX:
 *   
 *   D0 ──┬──╲                        D0 ──┬──╲
 *   D1 ──┴──╱──── Y                  D1 ──┤   ╲
 *           │                        D2 ──┤    ╲
 *          ╱╲                        D3 ──┴────╱──── Y
 *         S                                  ╱╲
 *                                          S1 S0
 *   Y = S̄·D0 + S·D1
 *                                    S1 S0 │ Y
 *   S │ Y                            ──────┼───
 *   ──┼───                            0  0 │ D0
 *   0 │ D0                            0  1 │ D1
 *   1 │ D1                            1  0 │ D2
 *                                     1  1 │ D3
 * 
 * MUX in datapaths: Selecting between ALU result, memory data, immediate, etc.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * DECODER
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Activates exactly one output based on input code.
 * n inputs → 2^n outputs (only one active at a time)
 * 
 *   2-to-4 Decoder:
 *                             A1 A0 │ Y0 Y1 Y2 Y3
 *   A1 ──┬──╲                 ──────┼────────────
 *   A0 ──┴──╱─┬─ Y0           0  0  │ 1  0  0  0
 *             ├─ Y1           0  1  │ 0  1  0  0
 *             ├─ Y2           1  0  │ 0  0  1  0
 *             └─ Y3           1  1  │ 0  0  0  1
 * 
 * Decoder in datapaths: Register file write select, memory address decoding
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * BUS NOTATION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * A BUS is a collection of wires carrying related signals (e.g., 32-bit data).
 * 
 *   Single wire:    ─────
 *   Bus (32-bit):   ═══/32═══
 * 
 * Addressing individual lines:
 *   Data[31:0]  - All 32 bits
 *   Data[7:0]   - Lower 8 bits (byte 0)
 *   Data[15:8]  - Bits 8-15 (byte 1)
 *   Data[31]    - Single bit (MSB/sign bit)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * SEQUENTIAL LOGIC: LATCHES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * SR LATCH (Set-Reset):
 *   - Level-sensitive (responds while inputs active)
 *   - Has forbidden state (S=R=1)
 * 
 *   ┌─────────────┐      S R │ Q  Q̄  │ Action
 *   │ S ─┬─[NOR]──┴── Q̄     ─────┼──────┼────────
 *   │    │    │             0 0 │ Q  Q̄  │ Hold
 *   │ R ─┴─[NOR]──┬── Q     0 1 │ 0  1  │ Reset
 *   │             │         1 0 │ 1  0  │ Set
 *   └─────────────┘         1 1 │ ?  ?  │ INVALID
 * 
 * D LATCH:
 *   - Level-sensitive
 *   - Eliminates forbidden state
 *   - Transparent when enable is high
 * 
 *   ┌─────────────────┐    E D │ Q
 *   │ D ──┬──[AND]──S │    ────┼───
 *   │     │           │    0 X │ Q (hold)
 *   │ E ──┼──[AND]──R │    1 0 │ 0
 *   │     │    │      │    1 1 │ 1
 *   │    [NOT]─┘      │
 *   └─────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * D FLIP-FLOP (Edge-Triggered)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * D FLIP-FLOP:
 *   - Edge-triggered (samples on clock edge)
 *   - Only changes on rising (or falling) clock edge
 *   - Foundation of synchronous digital systems
 * 
 *       ┌─────┐
 *   D ──┤     ├── Q
 *       │     │
 *   Clk─┤ >   ├── Q̄
 *       └─────┘
 * 
 *   On rising edge of Clk: Q ← D
 *   Otherwise: Q holds previous value
 * 
 * LATCH vs FLIP-FLOP:
 *   - Latch: Level-sensitive, transparent when enabled
 *   - Flip-flop: Edge-triggered, samples only at clock edge
 *   - Flip-flops preferred in synchronous design (more predictable timing)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * REGISTERS AND REGISTER FILES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * REGISTER (n-bit):
 *   n D flip-flops sharing the same clock
 *   
 *        ┌──────────────────────┐
 *   D[n] │ [D-FF] [D-FF] [D-FF] │ Q[n]
 *        └──────────┬───────────┘
 *                   │
 *                  Clk
 * 
 * REGISTER FILE (Figure A.8.8, A.8.9):
 *   Array of registers with read/write ports
 *   
 *   ┌─────────────────────────────────────────────┐
 *   │              REGISTER FILE                  │
 *   │  ┌──────────────────────────────────────┐   │
 *   │  │  Read        Write                   │   │
 *   │  │  Address     Address                 │   │
 *   │  │    │           │                     │   │
 *   │  │    ▼           ▼                     │   │
 *   │  │ ┌─────┐     ┌─────┐    Write         │   │
 *   │  │ │Decoder│   │Decoder│◄─ Enable       │   │
 *   │  │ └──┬──┘     └──┬──┘                  │   │
 *   │  │    │           │                     │   │
 *   │  │  ┌─┴───────────┴─┐                   │   │
 *   │  │  │   Register 0  │◄─ Write Data      │   │
 *   │  │  │   Register 1  │                   │   │
 *   │  │  │   Register 2  │                   │   │
 *   │  │  │      ...      │                   │   │
 *   │  │  │   Register 31 │                   │   │
 *   │  │  └───────┬───────┘                   │   │
 *   │  │          │                           │   │
 *   │  │       ┌──┴──┐                        │   │
 *   │  │       │ MUX │────► Read Data         │   │
 *   │  │       └─────┘                        │   │
 *   │  └──────────────────────────────────────┘   │
 *   └─────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * FINITE STATE MACHINES (FSM)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * FSM = Next State Logic + State Register + Output Logic
 * 
 *   ┌───────────────────────────────────────────────────────┐
 *   │                                                       │
 *   │   Inputs ──►┌──────────────┐   ┌──────────┐           │
 *   │             │  Next State  │──►│  State   │──┬──►─────┤
 *   │             │    Logic     │   │ Register │  │ Current│
 *   │             │ (combinat.)  │   │(flip-flop│  │ State  │
 *   │  ┌─────────►│              │   │)         │  │        │
 *   │  │          └──────────────┘   └──────────┘  │        │
 *   │  │                                  │        │        │
 *   │  │                                 Clk       │        │
 *   │  │                                           │        │
 *   │  └───────────────────────────────────────────┘        │
 *   │                                                       │
 *   │             ┌──────────────┐                          │
 *   │   Inputs ──►│   Output     │──────────────────► Output│
 *   │   State ───►│   Logic      │                          │
 *   │             │ (combinat.)  │                          │
 *   │             └──────────────┘                          │
 *   └───────────────────────────────────────────────────────┘
 * 
 * TYPES:
 *   Moore Machine: Output depends only on current state
 *   Mealy Machine: Output depends on current state AND inputs
 * 
 * MULTI-CYCLE PROCESSOR CONTROL:
 *   The control unit is an FSM where:
 *   - States = Processor stages (Fetch, Decode, Execute, Memory, WriteBack)
 *   - Inputs = Instruction opcode, ALU flags
 *   - Outputs = Control signals (RegWrite, MemRead, ALUOp, etc.)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CLOCK AND TIMING
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * CLOCK SIGNAL:
 *                    _____       _____       _____
 *   Clock: _____|     |_____|     |_____|     |_____
 *               ↑           ↑           ↑
 *            Rising      Rising      Rising
 *            Edge        Edge        Edge
 * 
 *   Clock Period (T) = 1 / Frequency
 *   Example: 3 GHz → T = 0.333 ns = 333 ps
 * 
 * SETUP AND HOLD TIMES:
 *   - Setup time (t_su): Data must be stable BEFORE clock edge
 *   - Hold time (t_h): Data must remain stable AFTER clock edge
 * 
 *            ╔══════╗
 *   Data ────╢ Stable ╟────────
 *            ╚══════╝
 *        ◄──t_su──►│◄─t_h─►
 *                  │
 *   Clock ─────────┘▲
 *                   │
 *                Rising edge
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CRITICAL PATH
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * CRITICAL PATH: Longest delay path through combinational logic between registers.
 * 
 * Determines maximum clock frequency:
 *   
 *   T_clock ≥ t_prop (reg) + t_comb (critical path) + t_setup
 * 
 *   ┌─────────┐      ┌─────────────────┐      ┌─────────┐
 *   │ Reg A   │──────│  Combinational  │──────│ Reg B   │
 *   │         │      │     Logic       │      │         │
 *   └────┬────┘      └────────┬────────┘      └────┬────┘
 *        │                    │                    │
 *       Clk                   │                   Clk
 *                             │
 *                   ◄─────────┴─────────►
 *                      Critical Path
 *                    (determines T_min)
 * 
 * SINGLE-CYCLE vs PIPELINED:
 *   Single-cycle: Critical path = entire instruction (long!)
 *   Pipelined: Critical path = slowest stage (shorter → faster clock)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class DigitalLogic {
    
    // ==================== BASIC GATES ====================
    
    public static boolean NOT(boolean a) { return !a; }
    public static boolean AND(boolean a, boolean b) { return a && b; }
    public static boolean OR(boolean a, boolean b) { return a || b; }
    public static boolean NAND(boolean a, boolean b) { return !(a && b); }
    public static boolean NOR(boolean a, boolean b) { return !(a || b); }
    public static boolean XOR(boolean a, boolean b) { return a ^ b; }
    public static boolean XNOR(boolean a, boolean b) { return !(a ^ b); }
    
    // ==================== COMBINATIONAL CIRCUITS ====================
    
    /**
     * 2-to-1 Multiplexer.
     */
    public static boolean mux2(boolean d0, boolean d1, boolean sel) {
        return sel ? d1 : d0;
    }
    
    /**
     * 4-to-1 Multiplexer.
     */
    public static boolean mux4(boolean d0, boolean d1, boolean d2, boolean d3, int sel) {
        switch (sel) {
            case 0: return d0;
            case 1: return d1;
            case 2: return d2;
            case 3: return d3;
            default: throw new IllegalArgumentException("sel must be 0-3");
        }
    }
    
    /**
     * 2-to-4 Decoder.
     * Returns an array where exactly one element is true.
     */
    public static boolean[] decoder2to4(boolean a1, boolean a0) {
        boolean[] outputs = new boolean[4];
        int index = (a1 ? 2 : 0) + (a0 ? 1 : 0);
        outputs[index] = true;
        return outputs;
    }
    
    /**
     * Half Adder: Adds two bits.
     * Returns {sum, carry}.
     */
    public static boolean[] halfAdder(boolean a, boolean b) {
        return new boolean[] {
            XOR(a, b),  // Sum
            AND(a, b)   // Carry
        };
    }
    
    /**
     * Full Adder: Adds three bits (including carry-in).
     * Returns {sum, carry-out}.
     */
    public static boolean[] fullAdder(boolean a, boolean b, boolean cin) {
        boolean[] ha1 = halfAdder(a, b);
        boolean[] ha2 = halfAdder(ha1[0], cin);
        return new boolean[] {
            ha2[0],                    // Sum
            OR(ha1[1], ha2[1])         // Carry-out
        };
    }
    
    // ==================== SEQUENTIAL CIRCUITS ====================
    
    /**
     * SR Latch (simulated).
     * State is maintained between calls.
     */
    public static class SRLatch {
        private boolean q = false;
        
        public boolean update(boolean s, boolean r) {
            if (s && r) {
                throw new IllegalStateException("Invalid state: S=1, R=1");
            } else if (s) {
                q = true;
            } else if (r) {
                q = false;
            }
            // else: hold
            return q;
        }
        
        public boolean getQ() { return q; }
    }
    
    /**
     * D Latch (simulated).
     * Transparent when enable is high.
     */
    public static class DLatch {
        private boolean q = false;
        
        public boolean update(boolean d, boolean enable) {
            if (enable) {
                q = d;
            }
            return q;
        }
        
        public boolean getQ() { return q; }
    }
    
    /**
     * D Flip-Flop (simulated).
     * Edge-triggered: updates on rising edge only.
     */
    public static class DFlipFlop {
        private boolean q = false;
        private boolean prevClock = false;
        
        public boolean update(boolean d, boolean clock) {
            // Rising edge detection
            if (clock && !prevClock) {
                q = d;
            }
            prevClock = clock;
            return q;
        }
        
        public boolean getQ() { return q; }
    }
    
    /**
     * N-bit Register (simulated).
     */
    public static class Register {
        private int value = 0;
        private final int bits;
        private boolean prevClock = false;
        
        public Register(int bits) {
            this.bits = bits;
        }
        
        public int update(int d, boolean clock) {
            if (clock && !prevClock) {
                value = d & ((1 << bits) - 1);  // Mask to bit width
            }
            prevClock = clock;
            return value;
        }
        
        public int getValue() { return value; }
    }
    
    // ==================== FSM EXAMPLE ====================
    
    /**
     * Example FSM: Simple traffic light controller.
     * Demonstrates Moore machine (output depends only on state).
     */
    public static class TrafficLightFSM {
        public enum State { GREEN, YELLOW, RED }
        public enum Output { GREEN_LIGHT, YELLOW_LIGHT, RED_LIGHT }
        
        private State currentState = State.GREEN;
        private int counter = 0;
        
        // State transition on clock tick
        public Output tick() {
            counter++;
            
            // Next state logic
            switch (currentState) {
                case GREEN:
                    if (counter >= 10) { currentState = State.YELLOW; counter = 0; }
                    break;
                case YELLOW:
                    if (counter >= 3) { currentState = State.RED; counter = 0; }
                    break;
                case RED:
                    if (counter >= 10) { currentState = State.GREEN; counter = 0; }
                    break;
            }
            
            // Output logic (Moore: depends only on state)
            switch (currentState) {
                case GREEN: return Output.GREEN_LIGHT;
                case YELLOW: return Output.YELLOW_LIGHT;
                case RED: return Output.RED_LIGHT;
                default: return Output.RED_LIGHT;
            }
        }
        
        public State getState() { return currentState; }
    }
    
    // ==================== TRUTH TABLE GENERATION ====================
    
    /**
     * Generate truth table for basic gates.
     */
    public static String generateTruthTables() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  TRUTH TABLES\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("NOT:          AND:         OR:          XOR:\n");
        sb.append("A│Y           A B│Y        A B│Y        A B│Y\n");
        sb.append("─┼─           ───┼─        ───┼─        ───┼─\n");
        sb.append("0│1           0 0│0        0 0│0        0 0│0\n");
        sb.append("1│0           0 1│0        0 1│1        0 1│1\n");
        sb.append("              1 0│0        1 0│1        1 0│1\n");
        sb.append("              1 1│1        1 1│1        1 1│0\n\n");
        
        sb.append("NAND:         NOR:         2-to-1 MUX:\n");
        sb.append("A B│Y         A B│Y        S D0 D1│Y\n");
        sb.append("───┼─         ───┼─        ───────┼───\n");
        sb.append("0 0│1         0 0│1        0  X  X│D0\n");
        sb.append("0 1│1         0 1│0        1  X  X│D1\n");
        sb.append("1 0│1         1 0│0\n");
        sb.append("1 1│0         1 1│0\n\n");
        
        // Decoder truth table
        sb.append("2-to-4 Decoder:\n");
        sb.append("A1 A0│Y3 Y2 Y1 Y0\n");
        sb.append("─────┼───────────\n");
        sb.append(" 0  0│ 0  0  0  1\n");
        sb.append(" 0  1│ 0  0  1  0\n");
        sb.append(" 1  0│ 0  1  0  0\n");
        sb.append(" 1  1│ 1  0  0  0\n\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate full adder operation.
     */
    public static String demonstrateFullAdder() {
        StringBuilder sb = new StringBuilder();
        sb.append("Full Adder Truth Table:\n");
        sb.append("A B Cin│Sum Cout\n");
        sb.append("───────┼────────\n");
        
        for (int a = 0; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                for (int c = 0; c <= 1; c++) {
                    boolean[] result = fullAdder(a==1, b==1, c==1);
                    sb.append(String.format("%d %d  %d │ %d   %d\n", 
                        a, b, c, result[0]?1:0, result[1]?1:0));
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * Demonstrate ripple-carry addition.
     */
    public static String demonstrateRippleCarryAdder(int a, int b, int bits) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ripple-Carry Adder: %d + %d (%d-bit)\n\n", a, b, bits));
        
        int carry = 0;
        int result = 0;
        
        sb.append("Bit│ A   B  Cin│Sum Cout\n");
        sb.append("───┼───────────┼────────\n");
        
        for (int i = 0; i < bits; i++) {
            int aBit = (a >> i) & 1;
            int bBit = (b >> i) & 1;
            boolean[] fa = fullAdder(aBit==1, bBit==1, carry==1);
            
            result |= (fa[0] ? 1 : 0) << i;
            sb.append(String.format(" %d │ %d   %d   %d │ %d   %d\n", 
                i, aBit, bBit, carry, fa[0]?1:0, fa[1]?1:0));
            carry = fa[1] ? 1 : 0;
        }
        
        sb.append(String.format("\nResult: %d (carry-out: %d)\n", result, carry));
        if (carry == 1) {
            sb.append("  (Overflow detected!)\n");
        }
        
        return sb.toString();
    }
    
    // ==================== MAIN DEMO ====================
    
    public static void main(String[] args) {
        System.out.println(generateTruthTables());
        System.out.println(demonstrateFullAdder());
        System.out.println(demonstrateRippleCarryAdder(12, 7, 4));
        System.out.println(demonstrateRippleCarryAdder(12, 7, 8));
        
        // FSM demo
        System.out.println("\nTraffic Light FSM (first 30 ticks):");
        TrafficLightFSM fsm = new TrafficLightFSM();
        for (int i = 0; i < 30; i++) {
            TrafficLightFSM.Output output = fsm.tick();
            System.out.printf("  Tick %2d: %s\n", i+1, output);
        }
    }
}

