import java.util.*;
import java.io.*;

public class Main {

    static int N;
    static int M;
    static int F;
    static int[][] map;
    static int[][][] timeMap; // 0: 동, 1: 서, 2: 남, 3: 북, 4: 윗면
    static int[][] d = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    static int[][] anomalyPoses; //r, c, d, v, cv
    static TimeMachine timeMachine;
    static Point wallP;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        F = Integer.parseInt(st.nextToken());
        map = new int[N][N];
        timeMap = new int[5][M][M];
        anomalyPoses = new int[F][5];


        boolean flag = true;
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
                if (map[i][j] == 3 && flag) {
                    wallP = new Point(i, j);
                    flag = false;
                }
            }
        }

        //시간의 벽 초기화
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < M; j++) {
                st = new StringTokenizer(br.readLine());
                for (int k = 0; k < M; k++) {
                    timeMap[i][j][k] = Integer.parseInt(st.nextToken());
                    if (timeMap[i][j][k] == 2) timeMachine = new TimeMachine(true, i, j, k);
                }
            }
        }

        //이상 현상 초기화
        for (int i = 0; i < F; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < 4; j++) {
                anomalyPoses[i][j] = Integer.parseInt(st.nextToken());
            }
            anomalyPoses[i][4] = anomalyPoses[i][3];
            map[anomalyPoses[i][0]][anomalyPoses[i][1]] = -1;
        }

//        for (int i = 0; i < N; i++) {
//            System.out.println(Arrays.toString(map[i]));
//        }System.out.println();
//        System.out.println(0 % 2);
        // System.out.println(wallP.r + ", " + wallP.c);
        System.out.println(bfs());
    }

    public static int bfs() {
        Deque<TimeMachine> q = new ArrayDeque<>();
        boolean[][] visitedPlan = new boolean[N][N];
        boolean[][][] visitedWall = new boolean[5][M][M];
        q.add(timeMachine);
        visitedWall[timeMachine.t][timeMachine.r][timeMachine.c] = true;

        int turn = 1;
        while (!q.isEmpty()) {
            flowTimeAnomaly();
//            for (int i = 0; i < N; i++) {
//                System.out.println(Arrays.toString(map[i]));
//            }System.out.println();
            int size = q.size();
//            System.out.println(turn);
            while (size-- > 0) {
                TimeMachine tm = q.poll();
//                System.out.println(tm + " | " + timeMap[tm.t][tm.r][tm.c]);

//                if (!tm.isWall && map[tm.r][tm.c] == 4) return turn;
                for (int i = 0; i < 4; i++) {
                    int tr = tm.r + d[i][0];
                    int tc = tm.c + d[i][1];

                    if (tm.isWall) { //시간의 벽에 위치함
                        Wall wall = getWillMovePlane(tm.t, tr, tc);
//                        System.out.println("AFTER = " + wall.t + ", " + wall.r + ", " + wall.c);
                        if (wall.t == 5) {
                            if (invalidRangePlane(wall.r, wall.c) || visitedPlan[wall.r][wall.c] || (map[wall.r][wall.c] == -1 || map[wall.r][wall.c] == 1)) continue;
                            if (map[tr][tc] == 4) return turn;
                            visitedPlan[wall.r][wall.c] = true;
                            q.add(new TimeMachine(wall.r, wall.c));
                        } else {
                            if (visitedWall[wall.t][wall.r][wall.c] || timeMap[wall.t][wall.r][wall.c] != 0) continue;
                            visitedWall[wall.t][wall.r][wall.c] = true;
                            q.add(new TimeMachine(true, wall.t, wall.r, wall.c));
                        }
                    } else {
                        if (invalidRangePlane(tr, tc) || visitedPlan[tr][tc] || map[tr][tc] == -1 || map[tr][tc] == 1) continue;
                        if (map[tr][tc] == 4) return turn;
                        visitedPlan[tr][tc] = true;
                        q.add(new TimeMachine(tr, tc));
                    }
                }
            }
            turn++;
        }
        return -1;
    }

    public static void flowTimeAnomaly() {
        for (int i = 0; i < F; i++) {
            if (invalidRangePlane(anomalyPoses[i][0], anomalyPoses[i][1])) continue;

            anomalyPoses[i][3]--;
            if (anomalyPoses[i][3] == 0) {
                anomalyPoses[i][3] = anomalyPoses[i][4];
                int tr = anomalyPoses[i][0] + d[anomalyPoses[i][2]][0];
                int tc = anomalyPoses[i][1] + d[anomalyPoses[i][2]][1];

                if (invalidRangePlane(tr, tc) || map[tr][tc] != 0) continue;

                anomalyPoses[i][0] = tr;
                anomalyPoses[i][1] = tc;
                map[tr][tc] = -1;
            }
        }
    }

    public static boolean invalidRangePlane(int r, int c) {
        return r < 0 || r >= N || c < 0 || c >= N;
    }

    public static Wall getWillMovePlane(int t, int r, int c) {
        if (0 <= r && r < M && 0 <= c && c < M) return new Wall(t, r, c);
//        System.out.println("BEFORE = " + t + ", " + r + ", " + c);
        // 0: 동, 1: 서, 2: 남, 3: 북, 4: 윗면, 5: 평면
        switch (t) {
            case 0:
                if (r < 0) return new Wall(4, M - c - 1, M - 1);
                else if (r >= M) return new Wall(5, wallP.r + M - c - 1, wallP.c + M);
                else if (c < 0) return new Wall(2, r, M - 1);
                else if (c >= M) return new Wall(3, r, 0);
                break;
            case 1:
                if (r < 0) return new Wall(4, c, 0);
                else if (r >= M) return new Wall(5, wallP.r + c, wallP.c - 1);
                else if (c < 0) return new Wall(3, r, M - 1);
                else if (c >= M) return new Wall(2, r, 0);
                break;
            case 2:
                if (r < 0) return new Wall(4, M - 1, c);
                else if (r >= M) return new Wall(5, wallP.r + M, wallP.c + c);
                else if (c < 0) return new Wall(1, r, M - 1);
                else if (c >= M) return new Wall(0, r, 0);
                break;
            case 3:
                if (r < 0) return new Wall(4, 0, M - c - 1);
                else if (r >= M) return new Wall(5, wallP.r - 1, wallP.c + M - c - 1);
                else if (c < 0) return new Wall(0, r, M - 1);
                else if (c >= M) return new Wall(1, r, 0);
                break;
            case 4:
                if (r < 0) return new Wall(3, 0, M - c - 1);
                else if (r >= M) return new Wall(2, 0, c);
                else if (c < 0) return new Wall(1, 0, r);
                else if (c >= M) return new Wall(0, 0, M - r - 1);
                break;
        }
        return null;
    }
}

class TimeMachine {
    boolean isWall;
    int t;
    int r;
    int c;

    public TimeMachine(boolean isWall, int t, int r, int c) {
        this.isWall = isWall;
        this.t = t;
        this.r = r;
        this.c = c;
    }

    public String toString() {
        return "TimeMachine{" +
                "isWall=" + isWall +
                ", t=" + t +
                ", r=" + r +
                ", c=" + c +
                '}';
    }
    public TimeMachine(int r, int c) {
        this.r = r;
        this.c = c;
    }
}

class Wall {
    int t;
    int r;
    int c;

    public Wall(int t, int r, int c) {
        this.t = t;
        this.r = r;
        this.c = c;
    }
}

class Point {
    int r;
    int c;

    public Point(int r, int c) {
        this.r = r;
        this.c = c;
    }
}