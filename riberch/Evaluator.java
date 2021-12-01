interface Evaluator
{
	public int evaluate(Board board);
}

class PerfectEvaluator implements Evaluator
{
	public int evaluate(Board board)
	{
		int discdiff
			= board.getCurrentColor()
			* (board.countDisc(Disc.BLACK) - board.countDisc(Disc.WHITE));
		
		return discdiff;
	}
}

class WLDEvaluator implements Evaluator
{
	public static final int WIN  =  1;
	public static final int DRAW =  0;
	public static final int LOSE = -1;

	public int evaluate(Board board)
	{
		int discdiff
			= board.getCurrentColor()
			* (board.countDisc(Disc.BLACK) - board.countDisc(Disc.WHITE));
		
		if(discdiff > 0) return WIN;
		else if(discdiff < 0) return LOSE;
		else return DRAW;
	}
}

