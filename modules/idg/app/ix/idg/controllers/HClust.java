package ix.idg.controllers;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Rajarshi Guha
 */
public class HClust {
    double[][] cdm, rdm;
    String[] colNames, rowNames;
    Cluster rcluster, ccluster;

    public HClust() {
    }

    Double[] getColumn(Double[][] matrix, int col) {
        Double[] ret = new Double[matrix.length];
        for (int i = 0; i < matrix.length; i++) ret[i] = matrix[i][col];
        return ret;
    }

    public void setData(Double[][] matrix, String[] colNames, String[] rowNames) {
        this.colNames = colNames;
        this.rowNames = rowNames;

        // compute distance matrix for rows
        rdm = new double[rowNames.length][rowNames.length];
        for (int i = 0; i < matrix.length - 1; i++) {
            rdm[i][i] = 0.0;
            for (int j = i + 1; j < matrix.length; j++)
                rdm[i][j] = distance(matrix[i], matrix[j]) ;
        }

        cdm = new double[colNames.length][colNames.length];
        for (int i = 0; i < matrix[0].length - 1; i++) {
            cdm[i][i] = 0.0;
            for (int j = i + 1; j < matrix[0].length; j++)
                cdm[i][j] = distance(getColumn(matrix, i),getColumn(matrix, j)) ;
        }

    }

    double distance(Double[] x, Double[] y) {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            double xx = x[i] == null ? 0 : x[i];
            double yy = y[i] == null ? 0 : y[i];
            sum += (xx - yy) * (xx - yy);
        }
        return Math.sqrt(sum);
    }

    public double[] getColumnClusteringHeights() {
        int n = 10;
        return getClusteringHeights(ccluster, n);
    }

    public double[] getRowClusteringHeights() {
        int n = 10;
        return getClusteringHeights(rcluster, n);
    }

    double[] getClusteringHeights(Cluster c, int n) {
        double[] ret = new double[n];
        double step = c.getDistanceValue() / 10.0;
        for (int i = 1; i <= n; i++) ret[i - 1] = step * i;
        return ret;
    }

    public TreeMap<String, Integer> getClusterMemberships(Cluster cluster, double height) {
        List<Cluster> disjoint = new ArrayList<Cluster>();
        recurse(cluster, height, disjoint);
        TreeMap<String, Integer> map = new TreeMap<>();
        for (int i = 0; i < disjoint.size(); i++) {
            Cluster c = disjoint.get(i);
            List<String> names = getLeafsForCluster(c);
            for (String name : names) map.put(name, i);
        }
        return map;
    }

    public void run() throws Exception {
        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        rcluster = alg.performClustering(rdm, rowNames, new AverageLinkageStrategy());
        ccluster = alg.performClustering(cdm, colNames, new AverageLinkageStrategy());

//        DendrogramPanel dp = new DendrogramPanel();
//        dp.setModel(rcluster);
//        JFrame f = new JFrame();
//        f.setContentPane(dp);
//        f.pack();
//        f.setVisible(true);

//        List<Cluster> disjoint = new ArrayList<Cluster>();
//        recurse(rcluster, 0.12 * 100, disjoint);
//        System.out.println("disjoint = " + disjoint.size());
//        for (Cluster c : disjoint) {
//            System.out.println("c = " + c + " " + c.getName() + " " + c.isLeaf());
//
//            List<String> n = getLeafsForCluster(c);
//            for (String s : n) System.out.print(s + " ");
//            System.out.println();
//        }
    }

    List<String> getLeafsForCluster(Cluster c) {
        List<String> rn = new ArrayList<>();
        recurse2(c, rn);
        return rn;
    }

    void recurse2(Cluster c, List<String> names) {
        if (c.isLeaf()) names.add(c.getName());
        else {
            for (Cluster child : c.getChildren()) {
                if (child.isLeaf()) names.add(child.getName());
                recurse2(child, names);
            }
        }
    }

    void recurse(Cluster c, double height, List<Cluster> ret) {
        for (Cluster cc : c.getChildren()) {
            if (cc.getTotalDistance() > height) {
                recurse(cc, height, ret);
            } else ret.add(cc);
        }
    }
}
