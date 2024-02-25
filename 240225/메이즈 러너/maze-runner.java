import java.util.*;
import java.io.*;

public class Main {

    static int N;
    static int K;
    static int M;
    static Point exitP;
    static Deque<Point> q = new ArrayDeque<>(); //참가자의 좌표를 담은 큐

    static Maps[][] map;
    static int distSum = 0;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());
        map = new Maps[N + 1][N + 1];

        //지도 초기화
        for (int i = 1; i <= N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 1; j <= N; j++) {
                map[i][j] = new Maps(Integer.parseInt(st.nextToken()) * -1);
            }
        }

        //참가자 좌표, 출구 초기화
        for (int i = 0; i <= M; i++) {
            st = new StringTokenizer(br.readLine());
            int y = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken());
            if (i == M) { //출구
                exitP = new Point(y, x);
                map[y][x] = new Maps(11);
            }
            else { //참가자
                map[y][x].addParticipant(i);
            }
        }

        //미로 탈출 시작
        while (K-- > 0) {
            move(); //참가자를 이동시킬 수 있으면 이동시킴

            if (M == 0) break; //모든 참가자가 탈출을 했다면
            searchRectangle(); //가장 작은 정사각형을 찾아서 회전
        }

        System.out.println(distSum);
        System.out.println(exitP);
    }

    //참가자 1명 이상과 출구를 포함한 가장 작은 정사각형 찾고 돌리기
    public static void searchRectangle() {

        for (int i = 1; i < N; i++) {
            for (int j = 1; j <= N - i; j++) {
                for (int k = 1; k <= N - i; k++) {
                    //출구와 참가자를 1명 이상 포함한다면 회전
                    if (containsPoint(j, k, i, exitP) && containsParticipant(j, k, i)) {
                        spin(j, k, i);
                        return;
                    }
                }
            }
        }
    }

    //정사각형 회전
    public static void spin(int r, int c, int n) {
        Maps[][] tmpMap = new Maps[n + 1][n + 1];
        Maps[][] copyMap = new Maps[n + 1][n + 1];

        //정사각형 복사
        for (int i = 0; i < n + 1; i++) {
            for (int j = 0; j < n + 1; j++) {
                tmpMap[i][j] = map[r + i][c + j];
            }
        }

        //정사각형 회전
        for (int i = 0; i < n + 1; i++) {
            for (int j = 0; j < n + 1; j++) {
                copyMap[i][j] = tmpMap[n - j][i];
                int naegudo = copyMap[i][j].naegudo;
                if (naegudo < 0) copyMap[i][j].naegudo++;
            }
        }

        //회전 결과 저장
        for (int i = 0; i < n + 1; i++) {
            for (int j = 0; j < n + 1; j++) {
                map[r + i][c + j] = copyMap[i][j];
                if (map[r + i][c + j].naegudo == 11) exitP.setPoint(r + i, c + j);
            }
        }
    }

    //정사각형에 포인트가 포함되는지
    public static boolean containsPoint(int r, int c, int n, Point p) {
        return r <= p.y && p.y <= r + n && c <= p.x && p.x <= c + n;
    }

    //정사각형에 참가자가 포함되는지
    public static boolean containsParticipant(int r, int c, int n) {
        for (int i = r; i <= r + n; i++) {
            for (int j = c; j <= c + n; j++) {
                if (!map[i][j].isEmpty()) return true;
            }
        }
        return false;
    }

    //참가자 이동
    public static void move() {
        int[][] d = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        Maps[][] copyMap = new Maps[N + 1][N + 1];
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                copyMap[i][j] = new Maps(map[i][j].naegudo);
            }
        }

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                if (!map[i][j].isEmpty()) {
                    boolean moveFlag = false;
                    int curDist = getDistance(i, j); //현재 위치에서 출구까지의 거리
                    for (int k = 0; k < 4; k++) {
                        int tx = j + d[k][1];
                        int ty = i + d[k][0];

                        //지도를 벗어나거나 벽이라면
                        if (invalidRange(tx, ty) || map[ty][tx].naegudo < 0) continue;

                        int willMoveDist = getDistance(ty, tx); //이동했을 때의 위치에서 출구까지의 거리
                        if (willMoveDist > curDist) continue;

                        moveFlag = true;
                        while (!map[i][j].isEmpty()) {
                            ++distSum;
                            int pNumber = map[i][j].poll();
                            if (willMoveDist == 0) M--;         //이동할 곳이 출구 라면
                            else copyMap[ty][tx].add(pNumber);  //현재 출구 거리보다 이동했을 때의 출구 거리가 더 작으면
                        }
                        break;
                    }
                    if (!moveFlag) { //참가자가 움직이지 않았다면
                        while (!map[i][j].isEmpty()) {
                            copyMap[i][j].add(map[i][j].poll());
                        }
                    }
                }
            }
        }
        map = copyMap;
    }

    public static boolean invalidRange(int x, int y) {
        return x <= 0 || x > N || y <= 0 || y > N;
    }

    //출구와 계산
    public static int getDistance(int y, int x) {
        return Math.abs(exitP.x - x) + Math.abs(exitP.y - y);
    }
}

class Maps {
    Deque<Integer> q = new ArrayDeque<>();
    int naegudo;

    Maps(int naegudo) {
        this.naegudo = naegudo;
    }

    public void addParticipant(int number) {
        q.add(number);
    }

    public boolean isEmpty() {
        return q.isEmpty();
    }

    public void add(int number) {
        q.add(number);
    }

    public int poll() {
        return q.poll();
    }
}

class Point {
    int x;
    int y;

    Point(int y, int x) {
        this.x = x;
        this.y = y;
    }

    public void setPoint(int y, int x) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return this.y + " " + this.x;
    }
}