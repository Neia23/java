import java.util.*;

class BookTest
{
	public static void main(String[] args)
	{
		BookManager book = new BookManager();
		Board board = new Board();
		Vector v;
		
		v = book.find(board);
		
		printvector(v);

		board.move(new Point("d3"));
		v = book.find(board);
		
		printvector(v);

		board.move(new Point("c3"));
		v = book.find(board);
		
		printvector(v);
	}
	
	static void printvector(Vector v)
	{
		for(int i=0; i<v.size(); i++)
		{
			System.out.print(v.get(i));
		}
		System.out.println();
	}
}
