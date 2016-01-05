package fr.eurecom.wifi3gproject;

public class ComputeThreshold {

	public static double avgSizeWifi;
	public static double avgSizeCell;
	static double[] sizeArray = {17.28, 19.2, 36, 10.96, 7.6, 17.28, 80, 160};

	public static double getThreshold(double q, double rateWifi,
			double rateCell, double epsilon, double Smin, double Smax,
			double Dmax, double lambda, double DWF, double media,
			double varianceiance) {

		double k = rateWifi / rateCell;
		double delta;
		double Dsigned;
		double tmp;
		int count = 0;
		double old_dsigned = 10000000;
		boolean exitD = true;

		if (k >= 1) {
			delta = Smin;
			tmp = delta;
		} else {
			delta = Smax;
			tmp = delta;
		}

		//double[] l = new double[2];
		//l = getParameters(q, media, variance);
		//Dsigned = computeDsigned(q, delta, lambdaOne, lambdaTwo, lambda, rateWifi, rateCell, k, DWF);
		Dsigned = computeDsignedUpdated(delta, lambda, rateWifi, rateCell, DWF);

		if (Dsigned < 0) {
			System.out.println("DELAY measured " + Dsigned);
			System.err.print("WRONG THRESHOLD!!!");
			return Smin;
		}
		
		System.out.println("DELAY before while "+ Dsigned + " DMAX: " + Dmax + " k: " + k 
							+ " RWF: " + rateWifi + " RCEL: " + rateCell + " DELTA: " + delta);
		
		while(Dsigned > Dmax && count < 200 && exitD){
			
			System.out.println("I am in the while ");
			
			if(k >= 1 && tmp <= Smax){
				tmp = tmp + epsilon;
				//Dsigned = computeDsigned(q, tmp, lambdaOne, lambdaTwo, lambda, rateWifi, rateCell, k, DWF);
				Dsigned = computeDsignedUpdated(tmp, lambda, rateWifi, rateCell, DWF);
				if (Dsigned > old_dsigned){
					exitD = false;
				}
				old_dsigned = Dsigned;
				System.out.println("k>1  Dsigned: " + Dsigned + " Delta: " + tmp);
			}
			
			if(k < 1 && tmp >= Smin){
				tmp = tmp - epsilon;
				//Dsigned = computeDsigned(q, tmp, lambdaOne, lambdaTwo, lambda, rateWifi, rateCell, k, DWF);
				Dsigned = computeDsignedUpdated(tmp, lambda, rateWifi, rateCell, DWF);
				if (Dsigned > old_dsigned){
					exitD = false;
				}
				old_dsigned = Dsigned;
				System.out.println("k<1  Dsigned: " + Dsigned + " Delta: " + tmp);
			}
			count++;
		}
		
		delta = tmp;

		return delta;
	}

	public static double computeDsigned(double q, double delta, double lambdaOne, double lambdaTwo, double lambda, double rateWifi,
			double rateCell, double k, double DWF){
		
		double a = (q * Math.exp((-1) * lambdaOne * delta)) * (delta + (1 / lambdaOne)) + (1-q) * Math.exp((-1) * lambdaTwo * delta) * (delta + (1 / lambdaTwo));
		double b =  k * rateCell;
		
		a = b / a;
		a = a - lambda;
		a = Math.pow(a, -1);
		
		double c = q * ((1/lambdaOne) - Math.exp((-1) * lambdaOne * delta) * (delta + (1 / lambdaOne))) + (1-q) * ((1 / lambdaTwo) - Math.exp((-1) * lambdaTwo * delta) * (delta + (1 / lambdaTwo)));
		double d = rateCell;
		
		c = d / c;
		c = c - lambda;
		c = Math.pow(c,-1);
		double e = DWF * (q * Math.exp((-1) * lambdaOne * delta) + (1-q) * Math.exp((-1) * lambdaTwo * delta));
		
		return a + c + e;
	}
	
	public static double computeDsignedUpdated(double delta, double lambda, double rateWifi, double rateCell, double DWF){
		
		int sumSizeWifi = 0;
		int sumSizeCell = 0;
		int counterWifi = 0;
		int counterCell = 0;
		double delayWifi = 0;
		double delayCell = 0;
		double avgSizeWifi = 0;
		double avgSizeCell = 0;

		for (double i : sizeArray) {

			if (i >= delta) {
				sumSizeWifi += i;
				counterWifi++;
			} else {
				sumSizeCell += i;
				counterCell++;
			}
		}
		
		if (sumSizeCell != 0) avgSizeCell = sumSizeCell / counterCell;
		if (sumSizeWifi != 0) avgSizeWifi = sumSizeWifi / counterWifi;
		
		if (sumSizeWifi != 0) delayWifi = 1 / ((rateWifi / avgSizeWifi) - lambda);
		if (sumSizeCell != 0) delayCell = 1 / ((rateCell / avgSizeCell) - lambda);
		
		System.out.println("S CELL:" + avgSizeCell);
		System.out.println("S WIFI:" + avgSizeWifi);
		System.out.println("D CELL:" + delayCell);
		System.out.println("D WIFI:" + delayWifi);
		
		return delayWifi + delayCell + ( counterWifi / (counterWifi + counterCell)) * DWF;
	}

}