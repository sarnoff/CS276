import java.util.ArrayList;


public class KFold {
	public ArrayList<MessageFeatures>[] test;
	public ArrayList<MessageFeatures>[] train;
	
//	public void print() {
//		System.out.println("Train: ");
//		for(int i = 0; i < train.length; i++) {
//			ArrayList<MessageFeatures> list = train[i];
//			System.out.println("Category "+i+" has "+list.size());
//			for(int j = 0; j < list.size(); j++){
//				
//			}			
//		}
//		
//		System.out.println("Test has "+test.length+" and train has "+train.length);
//		int ntest = 0;
//		int ntrain = 0;
//		for(int i = 0; i < train.length; i++) {
//			ArrayList<MessageFeatures> list = train[i];
//			ntrain += list.size();
//		}
//		for(int i = 0; i < test.length; i++) {
//			ArrayList<MessageFeatures> list = test[i];
//			ntest += list.size();
//		}
//		System.out.println("Test has "+ntest+" and train has "+ntrain);
//	}
}
