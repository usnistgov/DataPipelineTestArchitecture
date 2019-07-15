public class Main {

    public static void main(String[] args) throws Exception {
        //TestDriver test = new TestDriver();
        //test.traverseSequenceList();
        long start = System.nanoTime();
        FileReader fReader = new FileReader();
        fReader.readFromFile();
        long end = System.nanoTime();
        long total = start - end;
        printTimes(total);
    }

    private static void printTimes(long time){
        System.out.println();
        System.out.println("Total time in nanoseconds: " + time);
        System.out.println("Total time in milliseconds: " + (time / 1000000));
        System.out.println("Total time in seconds: " + ((time / 1000000) / 1000));
    }

}
