import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class Main {

    static int L;
    static int[][] map;
    static int[][] knightMap;
    static Knight[] knights;
    static int[][] delta = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // y, x

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        L = Integer.parseInt(st.nextToken());
        int N = Integer.parseInt(st.nextToken());
        int Q = Integer.parseInt(st.nextToken());
        map = new int[L][L];
        knightMap = new int[L][L];

        //지도 초기화
        for (int i = 0; i < L; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < L; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
                if (map[i][j] == 2) {
                    knightMap[i][j] = -1;
                }
            }
        }

        //기사의 정보 초기화
        knights = new Knight[N + 1];
        for (int i = 1; i <= N; i++) {
            st = new StringTokenizer(br.readLine());
            int r = Integer.parseInt(st.nextToken()) - 1;
            int c = Integer.parseInt(st.nextToken()) - 1;
            int h = Integer.parseInt(st.nextToken());
            int w = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            knights[i] = new Knight(i, r, c, h, w, k, knightMap);
            knightMap[r][c] = i;
        }

//        for (int i = 0; i < L; i++) {
//            System.out.println(Arrays.toString(knightMap[i]));
//        }

        //기사에게 명령 부여
        for (int i = 0; i < Q; i++) {
            st = new StringTokenizer(br.readLine());
            int knightNum = Integer.parseInt(st.nextToken());
            int dir = Integer.parseInt(st.nextToken());
            boolean[] visited = new boolean[N + 1];

            int dx = delta[dir][1];
            int dy = delta[dir][0];
            List<Integer> willMoveKnightList = new ArrayList<>();
            boolean isMove = knights[knightNum].isMove(knightMap, knights, visited, L, dx, dy, willMoveKnightList);

            if (isMove) {
                for (int willMoveKnightNum : willMoveKnightList) {
                    knights[willMoveKnightNum].fight(knightMap, dx, dy);
                }

                for (int willMoveKnightNum : willMoveKnightList) {
                    if (willMoveKnightNum == knightNum) continue;
                    knights[willMoveKnightNum].attacked(knightMap, map);
                }
            }
//            for (int j = 0; j < L; j++) {
//                System.out.println(Arrays.toString(knightMap[j]));
//            }
//            System.out.println(i);
        }

        int answer = 0;
        for (int i = 1; i <= N; i++) {
            answer += knights[i].getDamage();
        }
        System.out.println(answer);
//        System.out.println(2);
    }
}

class Knight {
    int n;
    int x;
    int y;
    int h;
    int w;
    int hp;
    int damage;

    public Knight(int n, int y, int x, int h, int w, int hp, int[][] knightMap) {
        this.n = n;
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
        this.hp = hp;
        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                knightMap[i][j] = n;
            }
        }
    }

    public int getDamage() {
        if (hp > 0) {
            return damage;
        }
        return 0;
    }

    public void attacked(int[][] knightMap, int[][] map) {
        int trapCnt = 0;

        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                if (map[i][j] == 1) {
                    trapCnt++;
                }
            }
        }

        hp -= trapCnt;
        damage += trapCnt;

        if (hp <= 0) {
            for (int i = y; i < y + h; i++) {
                for (int j = x; j < x + w; j++) {
                    knightMap[i][j] = 0;
                }
            }
        }
    }

    //밀쳐내기
    public void fight(int[][] knightMap, int dx, int dy) {


        int ty = y + dy;
        int tx = x + dx;

        //현재 위치를 0으로 바꾸고
        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                knightMap[i][j] = 0;
            }
        }
        y += dy;
        x += dx;

        //기사를 이동시킴
        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                knightMap[i][j] = n;
            }
        }
    }

    //기사가 이동 할 때 모든 다른 기사들이 움직일 수 있는지
    public boolean isMove(int[][] knightMap, Knight[] knights, boolean[] visited, int L, int dx, int dy, List<Integer> willMoveKnightList) {
        if (hp <= 0) {
            return false;
        }


        visited[n] = true;
        int ty = y + dy;
        int tx = x + dx;

        for (int i = ty; i < ty + h; i++) {
            for (int j = tx; j < tx + w; j++) {
                if (invalidRange(L, i, j) || isWall(knightMap, i, j)) {
                    return false;
                }
                if (knightMap[i][j] == 0) {
                    continue;
                }
                if (!visited[knightMap[i][j]] && !knights[knightMap[i][j]].isMove(knightMap, knights, visited, L, dx, dy, willMoveKnightList)) {
                    return false;
                }
            }
        }
        willMoveKnightList.add(n);
        return true;
    }

    //지도를 나가는지
    public boolean invalidRange(int L, int ty, int tx) {
        return tx < 0 || tx >= L || ty < 0 || ty >= L;
    }

    //벽이 있는지
    public boolean isWall(int[][] knightMap, int ty, int tx) {
        if (knightMap[ty][tx] == -1) {
            return true;
        }
        return false;
    }
}