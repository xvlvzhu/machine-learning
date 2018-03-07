package ML.DT;

import com.csvreader.CsvReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**决策树ID3算法
 * Created by zxl on 2018/3/6.
 */
public class DecisionTree {
    public List<String> feature = new ArrayList<>(); // 存放每个属性的名称
    public ArrayList<ArrayList<String>> featureName = new ArrayList(); // 存放每个属性的取值
    public ArrayList<String[]> data = new ArrayList(); // 存放数据
    public HashSet<Integer> featureIndex = new HashSet<>(); // 存放特征索引
    int h = 0; // 信息增益最小值选取

    // 初始化特征索引
    public void initialFeatureIndex(int N) {
        for (int i = 0; i < N; i++) {
            featureIndex.add(i);
        }
    }

    // 获取map中value最大的key
    public String getMaxValueKey(HashMap<String, Integer> m) {
        int maxNum = 0;
        String key = "";
        for (Map.Entry<String, Integer> entry : m.entrySet()) {
            if (entry.getValue() > maxNum) {
                maxNum = entry.getValue();
                key = entry.getKey();
            }
        }
        return key;
    }

    // 计算信息增益 （在C4.5中，只需将信息增益改成信息增益比即可）
    public int calInfoGain(ArrayList<String[]> di, LinkedHashMap<String, Integer> hs, HashSet<Integer> ai, double h) {
        int f = -1; //选取的分裂的特征
        // 计算H(D)
        double hd = 0.0;
        double datasize = (double) di.size();
        for (Map.Entry<String, Integer> entry : hs.entrySet()) {
            hd -= entry.getValue() / datasize * Math.log((double) entry.getValue() / datasize) / Math.log((double) 2);
        }
        System.out.println("hd" + hd);

        // 对每一个特征计算信息增益
        ArrayList<String> result = new ArrayList<>(hs.keySet());
        ArrayList<Integer> value = new ArrayList<>(hs.values());
        double hda = Double.MAX_VALUE;
        for (int i : ai) { //选取一个特征
            double feature_gain = 0.0;
            int[][] matrix = new int[hs.keySet().size()][featureName.get(i).size()]; // 构建特征类别矩阵
//            System.out.println("featureName"+featureName.get(i)+" "+i);
            for (String[] d : di) { //对于每一条数据
                int feature_idx = featureName.get(i).indexOf(d[i]);
                int class_idx = result.indexOf(d[d.length - 1]);
                matrix[class_idx][feature_idx] += 1;
            }

            int[] classnum = new int[matrix[0].length];
            for (int p = 0; p < matrix[0].length; p++) {
                int tmp = 0;
                for (int q = 0; q < matrix.length; q++) {
                    tmp += matrix[q][p];
                }
                classnum[p] = tmp;
            }

            for (int p = 0; p < matrix.length; p++) {
                for (int q = 0; q < matrix[0].length; q++) {
                    double ratio = (double) matrix[p][q] / (double) classnum[q];
//                    System.out.println("matrix[p][q]"+(double) matrix[p][q]);
//                    System.out.println("classnum[q]"+(double) classnum[q]);
//                    System.out.println("ratio"+ratio);
//                    System.out.println("classRatio"+(double)classnum[q] / di.size());

                    feature_gain -= (double) classnum[q] / di.size() * ratio * (ratio==0.0?0.0:Math.log(ratio)) / Math.log(2);
                }
            }
//            System.out.println("feature_gain"+feature_gain);
            if ((hd - feature_gain) > (hd - hda)) {
                hda = feature_gain;
                f = i;
            }
        }
        System.out.println("hda" + hda);
        System.out.println("gain" + (hd - hda));
        if (hd - hda > h) {
            return f;
        } else {
            return -1;
        }
    }

    // 构造决策树
    public void bulidTree(ArrayList<String[]> di, HashSet<Integer> ai, double h) {
        LinkedHashMap<String, Integer> hs = new LinkedHashMap();
        for (String[] i : di) {
            if (hs.containsKey(i[i.length - 1])) {
                hs.put(i[i.length - 1], hs.get(i[i.length - 1]) + 1);
            } else {
                hs.put(i[i.length - 1], 1);
            }
        }
        System.out.println("hs"+hs);
        // 获取最大类别
        String maxClass = getMaxValueKey(hs);

        // 数据集的类标种类是否只剩一种
        if (hs.size() == 1) {
            System.out.println("只剩一种" + maxClass);
            return;
        } else {
            // 特征集合中是否有可用特征
            if (ai.size() == 0) {
                System.out.println("无可用特征" + maxClass);
                return;
            } else {
                // 计算信息增益
                int feature_index = calInfoGain(di, hs, ai, h);
                System.out.println("feature_index"+feature_index);
                if (feature_index == -1) {
                    System.out.println(maxClass);
                    return;
                } else {
                    ai.remove(feature_index); // 特征集去除该特征
                    ArrayList<String> featureName_i = featureName.get(feature_index);
//                    System.out.println("featureName_i"+featureName_i);
                    for (String name : featureName_i) {
//                        System.out.println("name"+name);
                        ArrayList<String[]> subdata = new ArrayList<>();//生成新数据集
                        for (String[] data : di) {
//                            System.out.println("data[feature_index]"+data[feature_index]);
                            if (data[feature_index].equals(name)) {
                                subdata.add(data);
                            }
                        }
//                        System.out.println("subdata"+subdata);
                        bulidTree(subdata, ai, h);// 递归计算数据集
                    }

                }
            }
        }
    }

    public void id3() {
        bulidTree(data, featureIndex, h);
    }

    public void read() {
        String filePath = "./src/ML/DT/data.csv";
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath, ',', Charset.forName("UTF-8"));

            // 读表头
            csvReader.readHeaders();
            for (String temp : csvReader.getHeaders()) {
                feature.add(temp);
            }
//            feature = Arrays.asList(csvReader.getHeaders());
            feature.remove(feature.size() - 1);
            System.out.println(feature);
            initialFeatureIndex(feature.size());
            for (String i : feature) {
                featureName.add(new ArrayList<String>());
            }
            while (csvReader.readRecord()) {
                // 读一整行
//                System.out.println(csvReader.getRawRecord());
                // 读这行的某一列
//                System.out.println(csvReader.get("年龄"));
                String[] lineData = csvReader.getRawRecord().split(",");
                data.add(lineData);
                for (int i = 0; i < feature.size(); i++) {
                    if (!featureName.get(i).contains(csvReader.get(feature.get(i)))) {
                        featureName.get(i).add(csvReader.get(feature.get(i)));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        System.out.println(System.getProperty("user.dir"));
        DecisionTree dt = new DecisionTree();
        dt.read();
        System.out.println(dt.featureName);
        System.out.println(dt.featureIndex);
        for (String[] d : dt.data) {
            for (String d1 : d) {
                System.out.print(d1+" ");
            }
            System.out.println();
        }
        dt.id3();
    }

}
