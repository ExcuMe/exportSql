import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExportTxtUtil {

    /**
     * 导出
     *
     * @param file
     *            Txt文件(路径+文件名)，Txt文件不存在会自动创建
     * @param dataList
     *            数据
     * @return
     */
    public static boolean exportTxt(File file, List<String> dataList) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            return exportTxtByOS(out, dataList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 导出
     *
     * @param out
     *            输出流
     * @param dataList
     *            数据
     * @return
     */
    public static boolean exportTxtByOS(OutputStream out, List<String> dataList) {
        boolean isSucess = false;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            osw = new OutputStreamWriter(out);
            bw = new BufferedWriter(osw);
            // 循环数据
            for (int i = 0; i < dataList.size(); i++) {
                bw.append(dataList.get(i)).append("\r\n");
            }
            isSucess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isSucess = false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                    bw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                    osw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return isSucess;
    }

    public static void main(String[] args) throws IOException {
        List<String> list = new ArrayList<String>();
        list.add("Hello,World!");
        list.add("Hello,World!");
        list.add("Hello,World!");
        list.add("Hello,World!");

        String filePath = "D:/txt/";
        String fileName = java.util.UUID.randomUUID().toString().replaceAll("-", "")+".txt";
        File pathFile = new File(filePath);
        if(!pathFile.exists()){
            pathFile.mkdir();
        }
        String relFilePath = filePath + File.separator + fileName;
        File file = new File(relFilePath);
        if(!file.exists()){
            file.createNewFile();
        }
        boolean isSuccess=exportTxt(file,list);
        System.out.println(isSuccess);
    }

}