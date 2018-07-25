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
	private boolean yellowMove = true;
	private Disc[][] grid = new Disc[COLUMNS][ROWS];
	
	private Pane discRoot = new Pane();
	
	private Parent designLayout() 
	{
		Pane root = new Pane();
		root.getChildren().add(discRoot);
		
		Shape gridshape = makeGrid();
		root.getChildren().add(gridshape);
		root.getChildren().addAll(makecolumns());
		
		return root;
	}
	
	private Shape makeGrid() 
	{
		Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);
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
			
			rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200, 200, 50, 0.3)));
			rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));
			
			final int column = x;
			rect.setOnMouseClicked(e -> placeDisc(new Disc(yellowMove), column ));
			
			list.add(rect);		
		}
		
		return list;
	}
	
	private void placeDisc(Disc disc, int column) 
	{
		int row = ROWS - 1;
		do 
		{
			if (!getDisc(column, row).isPresent())
				break;
			row--;
		} while (row >=0);
		
		if (row < 0)
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
			return;
		}
			
		
		grid[column][row] = disc;
		discRoot.getChildren().add(disc);
		disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE / 4);
		
		final int currentRow = row;
		
		TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
		animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE / 4);
		animation.setOnFinished(e -> 
		{
			if (gameEnded(column, currentRow)) 
			{
				gameOver();
			}
			
			yellowMove = !yellowMove;
		});
		animation.play();
	}

	private void gameOver() 
	{
		String winner = yellowMove ? "Player1" : "Player2";
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
		return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonal1) || checkRange(diagonal2);
	}
	
	private boolean checkRange(List<Point2D> points) 
	{
		int chain = 0;
		
		for (Point2D p : points) {
			int column = (int) p.getX();
			int row = (int) p.getY();
			
			Disc disc = getDisc(column, row).orElse(new Disc(!yellowMove));
			if (disc.yellow == yellowMove) 
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
