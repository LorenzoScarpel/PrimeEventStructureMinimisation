public class FoldingPES {
    public static void main(String[] args) {
        String fileName = new CreateTest().createTestFile(5, 5, 4);
        long startTime = System.currentTimeMillis();
        PrimeEventStructure pes = new PrimeEventStructure(System.getProperty("user.dir") + "\\input\\"  + fileName);
        pes.generateMaximalFoldingEquivalence();
        System.out.println("Maximal folding equivalence (equivalent events on the same line):");
        pes.printMaximalFoldingEquivalence();
        long endTime = System.currentTimeMillis();
        System.out.println("Running time: " + (endTime - startTime) + " milliseconds");
    }
}
