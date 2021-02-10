import java.io.FileWriter;
import java.io.IOException;

public class CreateTest {
    public String createTestFile(Integer redundancy, Integer width, Integer depth){
        String fileName = "testPesRedundancy" + redundancy + "Width" + width + "Depth" + depth + ".txt";
        try {
            FileWriter testFile = new FileWriter(System.getProperty("user.dir") + "\\input\\"  + fileName);
            String alphabet = "abcdefghijklmnopqrstuvwxyz";
            for(int i=0; i<=depth; i++){
                for(int j=1; j<=width; j++){
                    for(int k=1; k<=redundancy; k++)
                        testFile.write(new StringBuilder().append(alphabet.charAt(i)).append(j).append(k).append(" ").append(alphabet.charAt(i))
                                .append(j).append("\n").toString());
                }
            }
            testFile.write("<\n");
            for(int i=0; i<depth; i++){
                for(int j=1; j<=width; j++){
                    for(int k=1; k<=redundancy; k++)
                        testFile.write(new StringBuilder().append(alphabet.charAt(i)).append(j).append(k).append(" ")
                                .append(alphabet.charAt(i+1)).append(j).append(k).append("\n").toString());
                }
            }
            testFile.write("#\n");
            for(int i=1; i<=width; i++){
                for(int j=1; j<redundancy; j++){
                    for(int k=j+1; k<=redundancy; k++)
                        testFile.write(new StringBuilder().append(alphabet.charAt(0)).append(i).append(j).append(" ")
                                .append(alphabet.charAt(0)).append(i).append(k).append("\n").toString());
                }
            }
            testFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
