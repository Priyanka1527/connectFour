package testgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JOptionPane;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Connect4App extends Application 
{

	public static final int TILE_SIZE = 60;
	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	
	private Stage primaryStage = null;
	private Disc[][] grid = new Disc[COLUMNS][ROWS];
	
	private int drawGame = 0;
	private int keepTrack[][] = new int[COLUMNS][ROWS];
	private boolean movePlayer1 = true;
	
	private Pane discRoot = new Pane();
	
	private Parent designLayout() 
	{
		//creating a new pane
		Pane root = new Pane();
	
		root.getChildren().add(discRoot);
		
		Shape gridshape = makeGrid();
		root.getChildren().add(gridshape);
		root.getChildren().addAll(makecolumns());
		
		return root;
	}
	
	private Shape makeGrid() 
	{
		//creating the field
		Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);
		
		//creating holed field
		for(int y = 0; y < ROWS; y++) 
		{
			for (int x = 0; x < COLUMNS; x++) 
			{
				Circle circle = new Circle(TILE_SIZE / 2);
				circle.setCenterX(TILE_SIZE / 2);
				circle.setCenterY(TILE_SIZE / 2);
				circle.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
				circle.setTranslateY(y * (TILE_SIZE + 5) + TILE_SIZE / 4);
				shape = Shape.subtract(shape, circle);
			}
		}
		
		//set the lighting and effects of the field
		Light.Distant light = new Light.Distant();
		light.setAzimuth(45.0);
		light.setElevation(30.0);
		
		Lighting lighting = new Lighting();
		lighting.setLight(light);
		lighting.setSurfaceScale(5.0);
		
		shape.setFill(Color.GREEN);
		shape.setEffect(lighting);
		
		return shape;
	}
	
	private List<Rectangle> makecolumns() 
	{
		List<Rectangle> list = new ArrayList<>();
		for (int x = 0; x < COLUMNS; x++) 
		{
			Rectangle rect = new Rectangle(TILE_SIZE, (ROWS + 1) * TILE_SIZE );
			rect.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
			rect.setFill(Color.TRANSPARENT);
			
			//denotes when hovering on a particular column
			rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200, 200, 50, 0.3)));
			rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));
			
			//setting the presently chosen column
			final int column = x;
			
			//calling function to place the disc on the selected column
			rect.setOnMouseClicked(e -> placeDisc(new Disc(movePlayer1), column ));
			list.add(rect);		
		}
		return list;
	}
	
	private void placeDisc(Disc disc, int column) 
	{
		int row = ROWS - 1;
		do 
		{
			//check if no disc is present
			if (!getDisc(column, row).isPresent())
				break;
			row--;
		} while (row >=0);
		
		if (row < 0)
		{
			return;		
		}
		
		//Place the disc of the selected column and empty row
		grid[column][row] = disc;
		
		//keeps track of the occupied cells
		keepTrack[column][row] = 1;
		
		for(int i = 0; i < ROWS; i++) 
		{
			for (int j = 0; j < COLUMNS; j++) 
			{
				if (keepTrack[j][i] == 1)
				{
					drawGame++;
				}
			}
		}
		//the counter drawGame becomes 903 when all the cells get filled
		//need to come up with a better approach to check this draw condition
		if (drawGame == 903)
		{
			JOptionPane.showMessageDialog(null, "It's a Draw!!");
			String choice = JOptionPane.showInputDialog("Do you want to continue ?");
			if(choice.equalsIgnoreCase("yes") || choice.equalsIgnoreCase("y"))
			{ 
				Connect4App newApp = new Connect4App();
				try 
				{
					newApp.start(new Stage());
					primaryStage.close();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				System.exit(0);
				return;
			}
		}
		
		discRoot.getChildren().add(disc);
		disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE / 4);
		
		final int currentRow = row;
		
		//dropping the disc and also setting the speed
		TranslateTransition animation = new TranslateTransition(Duration.seconds(0.1), disc);
		animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE / 4);
		animation.setOnFinished(e -> 
		{
			if (gameEnded(column, currentRow)) 
			{
				gameOver();
			}
			
			//changing player turn
			movePlayer1 = !movePlayer1;
		});
		animation.play();
	}

	private void gameOver() 
	{
		String winner = movePlayer1 ? "Player1" : "Player2";
		JOptionPane.showMessageDialog(null, "Winner: " + winner);
		String choice = JOptionPane.showInputDialog("Do you want to continue ?");
		if(choice.equalsIgnoreCase("yes") || choice.equalsIgnoreCase("y"))
		{ 
			Connect4App newApp = new Connect4App();
			try 
			{
				newApp.start(new Stage());
				primaryStage.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.exit(0);
			return;
		}
	}

	private boolean gameEnded(int column, int row) 
	{
		//Checking all the possible series e.g Vertical, Horizotal and the two diagonals
		List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3)
				.mapToObj(r -> new Point2D(column, r))
				.collect(Collectors.toList());
		
		List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(c -> new Point2D(c, row))
				.collect(Collectors.toList());
		
		Point2D topleft = new Point2D(column - 3, row - 3);
		List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> topleft.add(i, i))
				.collect(Collectors.toList());
		
		Point2D botleft = new Point2D(column - 3, row + 3);
		List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> botleft.add(i, -i))
				.collect(Collectors.toList());
		
		//returns true if either of the series gives 4
		return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonal1) || checkRange(diagonal2);
	}
	
	//Logic to check the four way series
	private boolean checkRange(List<Point2D> points) 
	{
		int chain = 0;
		
		for (Point2D p : points) {
			int column = (int) p.getX();
			int row = (int) p.getY();
			
			Disc disc = getDisc(column, row).orElse(new Disc(!movePlayer1));
			if (disc.yellow == movePlayer1) 
			{
				chain++;
				if(chain == 4) 
				{
					return true;
				}
			} else 
			{
				chain = 0;
			}
		}
		return false;
	}

	private Optional<Disc>  getDisc(int column, int row) 
	{
		if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS)
			return Optional.empty();
		return Optional.ofNullable(grid[column][row]);
	}

	@Override
	public void start(Stage stage) throws Exception 
	{
		stage.setScene(new Scene(designLayout()));
		stage.show();
		primaryStage = stage;
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
}
