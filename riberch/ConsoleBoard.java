class ConsoleBoard extends Board
{
	public void print()
	{
		System.out.println("  a b c d e f g h ");
		for(int y=1; y<=8; y++)
		{
			System.out.print(" " + y);
			for(int x=1; x<=8; x++)
			{
				switch(getColor(new Point(x, y)))
				{
				case Disc.BLACK:
					System.out.print("œ");
					break;
				case Disc.WHITE:
					System.out.print("›");
					break;
				default:
					System.out.print("@");
					break;
				}
			}
			System.out.println();
		}
	}
}
