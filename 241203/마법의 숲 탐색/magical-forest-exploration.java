import java.util.*;
import java.io.*;

public class Main {

    static int R;
    static int C;
    static int K;
    static int[][] map;
    static Golem[] golems;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());
        map = new int[R][C];
        golems = new Golem[K];
        int answer = 0;

        for (int i = 0; i < K; i++) {
            st = new StringTokenizer(br.readLine());
            int c = Integer.parseInt(st.nextToken()) - 1;
            int exit = Integer.parseInt(st.nextToken());
            golems[i] = new Golem(c, exit, i + 1);
        }

        //정령 내리기
        /* 정령 이동 우선순위
            1. 남쪽으로만
            2. 서쪽 후 남 이때 방향 (d - 1) % 4
            3. 동쪽 후 남 이때 방향 (d + 1) % 4
        */
        for (int i = 0; i < K; i++) {
            Golem golem = golems[i];

            while (true) {
                if (golem.isMoveSouth(R, C, map)) {
                    golem.goSouth(map);
                } else if (golem.isMoveWestAndSouth(R, C, map)) {
                    golem.goWest(map);
                    golem.goSouth(map);
                } else if (golem.isMoveEastAndSouth(R, C, map)) {
                    golem.goEast(map);
                    golem.goSouth(map);
                } else break;
            }
            // for(int j = 0; j < R; j++) {
            //     System.out.println(Arrays.toString(map[j]));
            // }System.out.println();
            if (golem.isOutBody(R, C)) {
                map = new int[R][C];
            } else {
                // System.out.println(golem.getLastRowNumber(R, C, map));
                // System.out.println("--------------\n");
                answer += golem.getLastRowNumber(R, C, map);
            }
        }
        System.out.println(answer);
    }
}

class Golem {
    Point p; //골렘 중앙 좌표
    int exit; //골렘 출구 방향
    int number; //골렘의 번호

    public Golem(int c, int exit, int number) {
        this.p = new Point(-2, c);
        this.exit = exit;
        this.number = number;
    }
    
    public int getR() {
        return this.p.r;
    }

    public int getC() {
        return this.p.c;
    }

    public Point getExitPoint() {
        switch (this.exit) {
            case 0:
            return new Point(this.getR() - 1, this.getC());

            case 1:
            return new Point(this.getR(), this.getC() + 1);
            
            case 2:
            return new Point(this.getR() + 1, this.getC());
            
            case 3:
            return new Point(this.getR(), this.getC() - 1);
        }
        return null;
    }

    public boolean isOutBody(int R, int C) {
        int topR = this.getR() - 1;
        return topR < 0;
    }

    public int getLastRowNumber(int R, int C, int[][] map) {
        Deque<Unit> q = new ArrayDeque<>();
        boolean[][] visited = new boolean[R][C];
        int[][] d = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        q.add(new Unit(this.getR(), this.getC(), this.number));
        visited[this.getR()][this.getC()] = true;
        int maxRowNumber = Integer.MIN_VALUE;

        while (!q.isEmpty()) {
            Unit unit = q.poll();

            maxRowNumber = Math.max(maxRowNumber, unit.r + 1);

            for (int i = 0; i < 4; i++) {
                int tr = unit.r + d[i][0];
                int tc = unit.c + d[i][1];

                if (tr < 0 || tr >= R || tc < 0 || tc >= C || visited[tr][tc]) continue;
                if (map[tr][tc] == unit.golemNumber) {
                    visited[tr][tc] = true;
                    q.add(new Unit(tr, tc, unit.golemNumber));
                } else if (map[tr][tc] < 0 && map[tr][tc] * -1 == unit.golemNumber) {
                    visited[tr][tc] = true;
                    q.add(new Unit(tr, tc, map[tr][tc]));
                } else if (unit.golemNumber < 0 && map[tr][tc] != 0) {
                    visited[tr][tc] = true;
                    q.add(new Unit(tr, tc, map[tr][tc]));
                }
            }
        }
        return maxRowNumber;
    }

    //이동 전 흔적 지우기
    public void removeGolemNumber(int[][] map) {
        int[][] d = {{0, 0}, {-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 5; i++) {
            int tr = this.getR() + d[i][0];
            int tc = this.getC() + d[i][1];
            if (tr < 0) continue;
            map[tr][tc] = 0;
        }
    }

    public boolean isMoveWestAndSouth(int R, int C, int[][] map) {
        if (!this.isMoveWest(R, C, map)) return false;
        Point tmpP = this.p;
        int exitTmp = this.exit;

        this.goWest(map);
        boolean isMoveSouthFlag = this.isMoveSouth(R, C, map);

        this.removeGolemNumber(map);
        this.p = tmpP;
        this.exit = exitTmp;
        int[][] d = {{0, 0}, {-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 5; i++) {
            int tr = this.getR() + d[i][0];
            int tc = this.getC() + d[i][1];
            if (tr < 0) continue;
            map[tr][tc] = this.number;
        }
        Point exitP = this.getExitPoint();
        if (exitP.r >= 0) map[exitP.r][exitP.c] = this.number * -1;

        return isMoveSouthFlag;
    }

    public boolean isMoveEastAndSouth(int R, int C, int[][] map) {
        if (!this.isMoveEast(R, C, map)) return false;
        Point tmpP = this.p;
        int exitTmp = this.exit;

        this.goEast(map);
        boolean isMoveSountFlag = this.isMoveSouth(R, C, map);

        this.removeGolemNumber(map);
        this.p = tmpP;
        this.exit = exitTmp;
        int[][] d = {{0, 0}, {-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 5; i++) {
            int tr = this.getR() + d[i][0];
            int tc = this.getC() + d[i][1];
            if (tr < 0) continue;
            map[tr][tc] = this.number;
        }
        Point exitP = this.getExitPoint();
        if (exitP.r >= 0) map[exitP.r][exitP.c] = this.number * -1;

        return isMoveSountFlag;
    }

    public boolean isMoveSouth(int R, int C, int[][] map) {
        //왼쪽, 오른쪽, 아래 이동 후포인트 좌표
        Point[] points = new Point[3];
        points[0] = new Point(this.getR() + 1, this.getC() - 1);
        points[1] = new Point(this.getR() + 1, this.getC() + 1);
        points[2] = new Point(this.getR() + 2, this.getC());

        boolean rangeFlag = true;
        boolean willMoveFlag = true;

        //범위를 충족하는지
        rangeFlag &= (points[2].r < R);
        if (!rangeFlag) return rangeFlag;

        //남쪽으로 갈 수 있는지
        for (int i = 0; i < 3; i++) {
            if (points[i].r < 0) continue;
            int mapNumber = map[points[i].r][points[i].c];
            if (mapNumber != 0 && mapNumber != this.number) {
                willMoveFlag = false;
                break;
            }
        }
        return willMoveFlag;
    }

    public void goSouth(int[][] map) {
        removeGolemNumber(map);
        this.p = new Point(this.getR() + 1, this.getC());
        int[][] d = {{0, 0}, {-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 5; i++) {
            int tr = this.getR() + d[i][0];
            int tc = this.getC() + d[i][1];
            if (tr < 0) continue;
            map[tr][tc] = this.number;
        }

        Point exitP = this.getExitPoint();
        if (exitP.r >= 0) map[exitP.r][exitP.c] = this.number * -1;
    }

    public boolean isMoveWest(int R, int C, int[][] map) {
        //왼쪽, 위, 아래 이동 후포인트 좌표
        Point[] points = new Point[3];
        points[0] = new Point(this.getR(), this.getC() - 2);
        points[1] = new Point(this.getR() - 1, this.getC() - 1);
        points[2] = new Point(this.getR() + 1, this.getC() - 1);

        boolean rangeFlag = true;
        boolean willMoveFlag = true;

        //범위를 충족하는지
        rangeFlag &= (points[0].c >= 0);
        if (!rangeFlag) return rangeFlag;
        
        //서쪽으로 갈 수 있는지
        for (int i = 0; i < 3; i++) {
            if (points[i].r < 0) continue;
            int mapNumber = map[points[i].r][points[i].c];
            if (mapNumber != 0 && mapNumber != this.number) {
                willMoveFlag = false;
                break;
            }
        }
        return willMoveFlag;
    }

    //서쪽 이동
    public void goWest(int[][] map) {
        this.exit = (this.exit - 1 + 4) % 4;

        removeGolemNumber(map);
        this.p = new Point(this.getR(), this.getC() - 1);
        int[][] d = {{0, 0}, {-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 5; i++) {
            int tr = this.getR() + d[i][0];
            int tc = this.getC() + d[i][1];
            if (tr < 0) continue;
            map[tr][tc] = this.number;
        }

        Point exitP = this.getExitPoint();
        if (exitP.r >= 0) map[exitP.r][exitP.c] = this.number * -1;
    }

    public boolean isMoveEast(int R, int C, int[][] map) {
        //오른쪽, 위, 아래 이동 후포인트 좌표
        Point[] points = new Point[3];
        points[0] = new Point(this.getR(), this.getC() + 2);
        points[1] = new Point(this.getR() - 1, this.getC() + 1);
        points[2] = new Point(this.getR() + 1, this.getC() + 1);

        boolean rangeFlag = true;
        boolean willMoveFlag = true;

        //범위를 충족하는지
        rangeFlag &= (points[0].c < C);
        if (!rangeFlag) return rangeFlag;

        //동쪽으로 갈 수 있는지
        for (int i = 0; i < 3; i++) {
            if (points[i].r < 0) continue;
            int mapNumber = map[points[i].r][points[i].c];
            if (mapNumber != 0 && mapNumber != this.number) {
                willMoveFlag = false;
                break;
            }
        }
        return willMoveFlag;
    }

    //동쪽 이동
    public void goEast(int[][] map) {
        this.exit = (this.exit + 1 + 4) % 4;

        removeGolemNumber(map);
        this.p = new Point(this.getR(), this.getC() + 1);
        int[][] d = {{0, 0}, {-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 5; i++) {
            int tr = this.getR() + d[i][0];
            int tc = this.getC() + d[i][1];
            if (tr < 0) continue;
            map[tr][tc] = this.number;
        }

        Point exitP = this.getExitPoint();
        if (exitP.r >= 0) map[exitP.r][exitP.c] = this.number * -1;
    }
}

class Unit {
    int r;
    int c;
    int golemNumber;

    public Unit(int r, int c, int golemNumber) {
        this.r = r;
        this.c = c;
        this.golemNumber = golemNumber;
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