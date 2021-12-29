package ix.core.chem;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.AtomCoordinates;
import gov.nih.ncats.molwitch.Bond;
import gov.nih.ncats.molwitch.Bond.Stereo;
import gov.nih.ncats.molwitch.Chemical;

import java.util.Optional;
import java.util.stream.IntStream;

public class ChemAligner {
    private ChemAligner() {
    }


    /**
     * Calculate the root mean squared distanceof the target molecule atoms 
     * to the query molecule given the atom mappings.  The map array is indexed by the 
     * atom index of ref, i.e., map[i] = j implies that the ith atom of query corresponds
     * to the jth atom of target where j >= 0, otherwise there is no mapping.
     */
    public static double rmsd(Chemical query, Chemical target, int[] map) {

        Atom[] qatoms = query.atoms().toArray(i->new Atom[i]);
        Atom[] tatoms = target.atoms().toArray(i->new Atom[i]);
        double totalsd=0;
        int c=0;
        for (int i = 0; i < map.length; ++i) {
            if (map[i] < 0) {
            }
            else {
                c++;
                Atom q = qatoms[i];
                Atom t = tatoms[map[i]];
                double dx = q.getAtomCoordinates().getX() - t.getAtomCoordinates().getX();
                double dy = q.getAtomCoordinates().getY() - t.getAtomCoordinates().getY();
                double dz = q.getAtomCoordinates().getZ().orElse(0) - t.getAtomCoordinates().getZ().orElse(0);
                double sd = dx*dx+dy*dy+dz*dz;
                totalsd+=sd;
            }
        }
        return Math.sqrt(totalsd/c);
    }

    /**
     * Clean the molecule, generating new coordinates but 
     * also attempting to rotate the cleaned version to be 
     * as close as possible to the old version.
     * 
     * This mutates the chemical object, and returns it 
     * as well.
     * @param c
     */
    public static Chemical align2DClean(Chemical c) {
//        Chemical qq=null;
//        try {
//            qq = Chemical.parse(c.toSd());
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//        
        Chemical q=c.copy(); 
        Chemical t=c;
        try{
            
            int[] map = IntStream.range(0, t.getAtomCount())
                    .map(i->{
                        Atom aa=q.getAtom(i);
//                        System.out.println(i+ "\t" + aa.getSymbol());
                        AtomCoordinates ac = aa.getAtomCoordinates();
                        if(ac!=null) {
                            return i;
                        }else {
//                            System.out.println("Don't map:" + i);
                            return -1;
                        }
                    })
                    .toArray();
            
            t.generateCoordinates(); 
            align(q,t, map);
        }catch(Exception e) {
            e.printStackTrace();
//            e.printStackTrace();
            //?
        }
        return t;
    }
    /**
     * Perform aligment of the target molecule given the query molecule
     * and the atom mappings.  The map array is indexed by the atom index
     * of ref, i.e., map[i] = j implies that the ith atom of query corresponds
     * to the jth atom of target where j >= 0, otherwise there is no mapping.
     */
    public static void align (Chemical query, Chemical target, int[] map) {
        if (query.getAtomCount() != map.length) {
            throw new IllegalArgumentException 
            ("Input mapping does match reference molecule size");
        }
//        if (!query.has2DCoordinates()) {
//            try {
//                query.generateCoordinates();
//            } catch (MolwitchException e) {
//                throw new RuntimeException(e);
//            }
//        }
        int size = 0;
        double qx = 0., qy = 0., qz = 0.;
        double tx = 0., ty = 0., tz = 0.;
        Atom[] qatoms = query.atoms().toArray(i->new Atom[i]);
        Atom[] tatoms = target.atoms().toArray(i->new Atom[i]);
        for (int i = 0; i < map.length; ++i) {
            if (map[i] < 0) {
            }
            else {
                Atom q = qatoms[i];
                Atom t = tatoms[map[i]];
                qx += q.getAtomCoordinates().getX();
                qy += q.getAtomCoordinates().getY();
                qz += q.getAtomCoordinates().getZ().orElse(0);
                tx += t.getAtomCoordinates().getX();
                ty += t.getAtomCoordinates().getY();
                tz += t.getAtomCoordinates().getZ().orElse(0);
                ++size;
            }
        }
        qx /= size;
        qy /= size;
        qz /= size;
        tx /= size;
        ty /= size;
        tz /= size;
        // now center the vectors
        DoubleMatrix2D Y = DoubleFactory2D.dense.make(3, size);
        DoubleMatrix2D X = DoubleFactory2D.dense.make(3, size);
        for (int i = 0, j = 0; i < map.length; ++i) {
            if (map[i] < 0) {
            }
            else {
                Atom q = qatoms[i];
                Atom t = tatoms[map[i]];
                X.setQuick(0, j, q.getAtomCoordinates().getX() - qx);
                X.setQuick(1, j, q.getAtomCoordinates().getY() - qy);
                X.setQuick(2, j, q.getAtomCoordinates().getZ().orElse(0) - qz);
                Y.setQuick(0, j, t.getAtomCoordinates().getX() - tx);
                Y.setQuick(1, j, t.getAtomCoordinates().getY() - ty);
                Y.setQuick(2, j, t.getAtomCoordinates().getZ().orElse(0) - tz);
                ++j;
            }
        }
        //System.out.println("Y = " + Y);
        //System.out.println("X = " + X);
        Algebra alg = new Algebra ();
        // now compute A = YX'
        DoubleMatrix2D A = alg.mult(Y, alg.transpose(X));	
        //System.out.println("A = " + A);
        SingularValueDecomposition svd = new SingularValueDecomposition(A);

        DoubleMatrix2D U = svd.getU();
        DoubleMatrix2D V = svd.getV();
        DoubleMatrix2D R = alg.mult(U, alg.transpose(V));
//                System.out.println("R = " + R);
        //I'm not 100% sure, but I think this can actually give the exact wrong
        //answer too
        
        R = alg.transpose(R);
        for (Atom a : tatoms) {
            Optional<AtomCoordinates> ac = Optional.ofNullable(a.getAtomCoordinates());
            double x = ac.map(aa->aa.getX()).orElse(0.0)-tx;
            double y = ac.map(aa->aa.getY()).orElse(0.0)-ty;
            double z = ac.map(aa->aa.getZ().orElse(0.0)).orElse(0.0)-tz;
            double rx = x*R.getQuick(0, 0) 
                    + y*R.getQuick(0, 1) + z*R.getQuick(0, 2)+qx;
            double ry = x*R.getQuick(1, 0) 
                    + y*R.getQuick(1, 1) + z*R.getQuick(1, 2)+qy;
            double rz = x*R.getQuick(2, 0) 
                    + y*R.getQuick(2, 1) + z*R.getQuick(2, 2)+qz;
            
            a.setAtomCoordinates(AtomCoordinates.valueOf(rx, ry));
            //	    .setXYZ(rx, ry, rz);
        }
        //if rotoinversion, invert dashes and wedges
        double det = R.getQuick(0, 0)*R.getQuick(1, 1)-R.getQuick(0, 1)*R.getQuick(1, 0);
        if(det<0){
            for(Bond mb:target.getBonds()){
                if(mb.getBondType().getOrder()==1 && !mb.getStereo().equals(Stereo.NONE)){
                    mb.setStereo(mb.getStereo().flip());
                }
            }
        }
    }
}
