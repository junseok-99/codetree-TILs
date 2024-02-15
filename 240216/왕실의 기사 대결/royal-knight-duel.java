import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Main {

    static int L;
    static int[][] map;        //지도(함정, 벽)
    static int[][] knightMap; //기사의 영역
    static Knight[] knights; //기사들
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
                if (map[i][j] == 2) { //벽이면
                    knightMap[i][j] = -1;
                }
            }
        }

        //기사의 정보 초기화후 배치
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

        //기사에게 명령 부여
        for (int i = 0; i < Q; i++) {
            st = new StringTokenizer(br.readLine());
            int knightNum = Integer.parseInt(st.nextToken());
            int dir = Integer.parseInt(st.nextToken());
            boolean[] visited = new boolean[N + 1];

            int dx = delta[dir][1];
            int dy = delta[dir][0];
            List<Integer> willMoveKnightList = new ArrayList<>(); //움직일 기사들의 번호들
            boolean isMove = knights[knightNum].isMove(knightMap, knights, visited, L, dx, dy, willMoveKnightList);

            //모든 기사들이 움직일 수 있는 조건이라면
            if (isMove) {
                //모든 기사들을 움직임
                for (int willMoveKnightNum : willMoveKnightList) {
                    knights[willMoveKnightNum].move(knightMap, dx, dy);
                }

                //명령을 부여한 기사를 제외하고 움직인 후 영역에 트랩 개수만큼 데미지를 받음
                for (int willMoveKnightNum : willMoveKnightList) {
                    if (willMoveKnightNum == knightNum) continue;
                    knights[willMoveKnightNum].attacked(knightMap, map);
                }
            }
        }

        int answer = 0;
        for (int i = 1; i <= N; i++) {
            answer += knights[i].getDamage();
        }
        System.out.println(answer);
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

    //생존한 기사의 데미지
    public int getDamage() {
        if (hp > 0) {
            return damage;
        }
        return 0;
    }

    //기사의 영역 내 트랩 수 만큼 데미지 받음
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

        //기사가 죽으면 영역에서 제외
        if (hp <= 0) {
            for (int i = y; i < y + h; i++) {
                for (int j = x; j < x + w; j++) {
                    knightMap[i][j] = 0;
                }
            }
        }
    }

    //기사 움직이기
    public void move(int[][] knightMap, int dx, int dy) {
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
        if (hp <= 0) { //기사가 죽어있다면 패스
            return false;
        }

        visited[n] = true;
        int ty = y + dy;
        int tx = x + dx;

        for (int i = ty; i < ty + h; i++) {
            for (int j = tx; j < tx + w; j++) {
                //기사가 움직일 곳이 지도를 벗어나거나 벽이 있다면
                if (invalidRange(L, i, j) || isWall(knightMap, i, j)) {
                    return false;
                }
                //0은 빈 곳이므로 패스
                if (knightMap[i][j] == 0) {
                    continue;
                }
                //기사가 움직일 곳에 다른 기사가 있는데, 그 기사가 isMove()에 만족하지 않으면(재귀)
                if (!visited[knightMap[i][j]] &&
                    !knights[knightMap[i][j]].isMove(knightMap, knights, visited, L, dx, dy, willMoveKnightList))
                {
                    return false;
                }
            }
        }

        //제일 끝에 있는 기사의 번호부터 추가
        //앞에부터 넣으면 이동시 앞 기사의 번호가 삭제됨
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