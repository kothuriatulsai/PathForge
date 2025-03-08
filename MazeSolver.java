import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.Stack;


public class MazeSolver extends JPanel {
    private static final int SIZE = 20;
    private static final int CELL_SIZE = 30;
    private int[][] maze;
    private Point start, end;
    private List<Point> path;
    private JButton bfsButton, dfsButton, generateButton;
    private JLabel distanceLabel, timeLabel;
    
    public MazeSolver() {
        maze = new int[SIZE][SIZE];
        generateMaze();
        setLayout(new BorderLayout());

        bfsButton = new JButton("Solve BFS");
        bfsButton.addActionListener(e -> solveMaze("BFS"));

        dfsButton = new JButton("Solve DFS");
        dfsButton.addActionListener(e -> solveMaze("DFS"));
        
        generateButton = new JButton("Generate New Maze");
        generateButton.addActionListener(e -> {
            generateMaze();
            start = null;
            end = null;
            path = null;
            distanceLabel.setText("Distance: ");
            timeLabel.setText("Time: ");
            repaint();
        });
        
        distanceLabel = new JLabel("Distance: ");
        timeLabel = new JLabel("Time: ");

        JPanel controlPanel = new JPanel();
        controlPanel.add(bfsButton);
        controlPanel.add(dfsButton);
        controlPanel.add(generateButton);
        controlPanel.add(distanceLabel);
        controlPanel.add(timeLabel);
        add(controlPanel, BorderLayout.NORTH);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int x = e.getY() / CELL_SIZE;
                int y = e.getX() / CELL_SIZE;
                if (x < SIZE && y < SIZE) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        start = new Point(x, y);
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        end = new Point(x, y);
                    }
                    repaint();
                }
            }
        });
    }

    private void generateMaze() {
        Random rand = new Random();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                maze[i][j] = rand.nextDouble() < 0.3 ? 1 : 0;
            }
        }
    }

    private void solveMaze(String algorithm) {
        if (start != null && end != null) {
            long startTime = System.nanoTime();
            if (algorithm.equals("BFS")) path = bfs(start, end);
            else if (algorithm.equals("DFS")) path = dfs(start, end);
            long endTime = System.nanoTime();
            updateDistanceAndTime((endTime - startTime) / 1e6);
            repaint();
        }
    }

    private List<Point> bfs(Point start, Point end) {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Map<Point, Point> parent = new HashMap<>();
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited[start.x][start.y] = true;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) return reconstructPath(parent, end);
            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i], ny = current.y + dy[i];
                if (isValid(nx, ny, visited)) {
                    Point next = new Point(nx, ny);
                    queue.add(next);
                    visited[nx][ny] = true;
                    parent.put(next, current);
                }
            }
        }
        return null;
    }

    private List<Point> dfs(Point start, Point end) {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Map<Point, Point> parent = new HashMap<>();
        Stack<Point> stack = new Stack<>();
        stack.push(start);
        visited[start.x][start.y] = true;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!stack.isEmpty()) {
            Point current = stack.pop();
            if (current.equals(end)) return reconstructPath(parent, end);
            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i], ny = current.y + dy[i];
                if (isValid(nx, ny, visited)) {
                    Point next = new Point(nx, ny);
                    stack.push(next);
                    visited[nx][ny] = true;
                    parent.put(next, current);
                }
            }
        }
        return null;
    }

    private boolean isValid(int x, int y, boolean[][] visited) {
        return x >= 0 && y >= 0 && x < SIZE && y < SIZE && !visited[x][y] && maze[x][y] == 0;
    }

    private List<Point> reconstructPath(Map<Point, Point> parent, Point end) {
        List<Point> path = new ArrayList<>();
        for (Point at = end; at != null; at = parent.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private void updateDistanceAndTime(double time) {
        if (path != null) {
            distanceLabel.setText("Distance: " + path.size() + " units");
            timeLabel.setText("Time: " + String.format("%.3f ms", time));
        } else {
            distanceLabel.setText("Distance: N/A");
            timeLabel.setText("Time: N/A");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                g.setColor(maze[i][j] == 1 ? Color.BLACK : Color.WHITE);
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        if (path != null) {
            g.setColor(Color.BLUE);
            for (Point p : path) {
                g.fillRect(p.y * CELL_SIZE, p.x * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        if (start != null) {
            g.setColor(Color.GREEN);
            g.fillRect(start.y * CELL_SIZE, start.x * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
        if (end != null) {
            g.setColor(Color.RED);
            g.fillRect(end.y * CELL_SIZE, end.x * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Solver");
            MazeSolver mazeSolver = new MazeSolver();
            frame.add(mazeSolver);
            frame.setSize(SIZE * CELL_SIZE + 50, SIZE * CELL_SIZE + 100);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
