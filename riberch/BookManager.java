import java.util.*;
import java.io.*;

class CoordinatesTransformer
{
	private int Rotate = 0;
	private boolean Mirror = false;

	public CoordinatesTransformer(Point first)
	{
		if(first.equals(new Point("d3")))
		{
			Rotate = 1;
			Mirror = true;
		}
		else if(first.equals(new Point("c4")))
		{
			Rotate = 2;
		}
		else if(first.equals(new Point("e6")))
		{
			Rotate = -1;
			Mirror = true;
		}

	}
	
	// 座標をf5を開始点とする座標系に正規化する
	public Point normalize(Point p)
	{
		Point newp = rotatePoint(p, Rotate);
		if(Mirror) newp = mirrorPoint(newp);
		
		return newp;
	}
	
	// f5を開始点とする座標を本来の座標に戻す
	public Point denormalize(Point p)
	{
		Point newp = new Point(p.x, p.y);
		if(Mirror) newp = mirrorPoint(newp);
		
		newp = rotatePoint(newp, -Rotate);
		
		return newp;
	}

	private Point rotatePoint(Point old_point, int rotate)
	{
		rotate %= 4;
		if(rotate < 0) rotate += 4;

		Point new_point = new Point();
		
		switch(rotate)
		{
		case 1:
			new_point.x = old_point.y;
			new_point.y = Board.BOARD_SIZE - old_point.x+1;
			break;
		case 2:
			new_point.x = Board.BOARD_SIZE - old_point.x+1;
			new_point.y = Board.BOARD_SIZE - old_point.y+1;
			break;
		case 3:
			new_point.x = Board.BOARD_SIZE - old_point.y+1;
			new_point.y = old_point.x;
			break;
		default: // 0
			new_point.x = old_point.x;
			new_point.y = old_point.y;
			break;
		}
		
		return new_point;
	}

	private Point mirrorPoint(Point point)
	{
		Point new_point = new Point();
		new_point.x = Board.BOARD_SIZE - point.x + 1;
		new_point.y = point.y;

		return new_point;
	}
}

class BookManager
{
	
	private static final String BOOK_FILE_NAME = "reversi.book";

	class Node
	{
		public Node child = null;
		public Node sibling = null;
		// public int eval = 0;
		public Point point = new Point();
	}
	
	private Node Root = null;

	public BookManager()
	{
		Root = new Node();
		Root.point = new Point("f5");
		
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(BOOK_FILE_NAME);
		}
		catch(FileNotFoundException e)
		{
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		
		String line;
		try
		{
			while((line = br.readLine()) != null)
			{
				Vector book = new Vector();
				for(int i=0; i<line.length(); i+=2)
				{
					Point p = null;
					try
					{
						p = new Point(line.substring(i));
					}
					catch(IllegalArgumentException e)
					{
						break;
					}

					book.add(p);
				}
				
				add(book);
			}
		}
		catch(IOException e) {}
	}

	public Vector find(Board board)
	{
		Node node = Root;
		Vector history = board.getHistory();
		
		if(history.isEmpty()) return board.getMovablePos();
		
		Point first = (Point) history.get(0);
		CoordinatesTransformer transformer = new CoordinatesTransformer(first);

		// 座標を変換してf5から始まるようにする
		Vector normalized = new Vector();
		for(int i=0; i<history.size(); i++)
		{
			Point p = (Point) history.get(i);
			p = transformer.normalize(p);

			normalized.add(p);
		}
		

		// 現在までの棋譜リストと定石の対応を取る
		for(int i=1; i<normalized.size(); i++)
		{
			Point p = (Point) normalized.get(i);

			node = node.child;
			while(node != null)
			{
				if(node.point.equals(p)) break;

				node = node.sibling;
			}
			if(node == null)
			{
				// 定石を外れている
				return board.getMovablePos();
			}
		}
		
		// 履歴と定石の終わりが一致していた場合
		if(node.child == null) return board.getMovablePos();

		Point next_move = getNextMove(node);
		
		// 座標を元の形に変換する
		next_move = transformer.denormalize(next_move);
		
		Vector v = new Vector();
		v.add(next_move);
		
		return v;

	}
	
	private Point getNextMove(Node node)
	{
		Vector candidates = new Vector();
		
		for(Node p=node.child; p != null; p = p.sibling)
		{
			candidates.add(p.point);
		}

		int index = (int)(Math.random() * candidates.size());
		Point point = (Point) candidates.get(index);

		return new Point(point.x, point.y);
	}
	
	private void add(Vector book)
	{
		Node node = Root;

		for(int i=1; i<book.size(); i++)
		{
			Point p = (Point) book.get(i);

			if(node.child == null)
			{
				// 新しい定石手
				node.child = new Node();
				node = node.child;
				node.point.x = p.x;
				node.point.y = p.y;
			}
			else
			{
				// 兄弟ノードの探索に移る
				node = node.child;

				while(true)
				{
					// 既にこの手はデータベース中にあり、その枝を見つけた
					if(node.point.equals(p)) break;

					// 定石木の新しい枝
					if(node.sibling == null)
					{
						node.sibling = new Node();

						node = node.sibling;
						node.point.x = p.x;
						node.point.y = p.y;
						break;
					}
					
					node = node.sibling;
				}
			}
		}
	}


}
