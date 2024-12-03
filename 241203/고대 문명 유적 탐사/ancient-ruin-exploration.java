import java.util.*;
import java.io.*;

/*
    - 회전 목표의 최대화 구하기
    - 유물 획득 (3개 이상 연결된 경우)
    - 벽면 채우기
*/
public class Main {

    static int K;
    static int M;
    static int[][] map;
    static int MAP_LEN = 5;
    static Deque<Integer> wallNumbers;
    static int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static int answer;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        K = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        map = new int[MAP_LEN][MAP_LEN];
        wallNumbers = new ArrayDeque<>();
        answer = 0;
        StringBuilder sb = new StringBuilder();

        //지도 초기화
        for (int i = 0; i < MAP_LEN; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < MAP_LEN; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        //유적의 벽면 숫자 초기화
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < M; i++) {
            wallNumbers.add(Integer.parseInt(st.nextToken()));
        }

        while (K-- > 0) {
            //모든 경우의 수 계산 후 최선의 정보 획득
            RotateInfo rotateInfo = calcRotateInfo();
            if (rotateInfo.score == 0) break;

            //1차 유물 획득
            map = rotateInfo.map;
            answer = calcScore(map, 1);

            //획득한 유물 벽면의 숫자 채우기
            do {
                fillWallNumber();
                answer += calcScore(map, 1);
            } 
            while (calcScore(map, 0) > 0);
            sb.append(answer).append(' ');
        }
        System.out.println(sb);
    }

    public static void fillWallNumber() {
        for (int i = 0; i < MAP_LEN; i++) {
            for (int j = MAP_LEN - 1; j >= 0; j--) {
                if (map[j][i] == -1) {
                    map[j][i] = wallNumbers.poll();
                }
            }
        }
    }

    //각 좌표마다 90, 180, 270 회전 후 얻을 수 있는 유물 획득 객체 생성
    public static RotateInfo calcRotateInfo() {
        PriorityQueue<RotateInfo> pq = new PriorityQueue<>();
        int[][] srcMap;
        for (int i = 1; i < MAP_LEN - 1; i++) {
            for (int j = 1; j < MAP_LEN - 1; j++) {
                srcMap = map;
                for (int k = 90; k <= 270; k += 90) {
                    int[][] tmpMap = getRotatedMap(i - 1, j - 1, srcMap);
                    int willSaveScore = calcScore(tmpMap, 0);
                    pq.add(new RotateInfo(willSaveScore, k, i, j, tmpMap));
                    srcMap = tmpMap;
                }
            }
        }
        return pq.poll();
    }

    //90도 회전된 맵 생성
    public static int[][] getRotatedMap(int r, int c, int[][] srcMap) {
        int[][] tmpMap = new int[MAP_LEN][MAP_LEN];
        
        for (int i = 0; i < MAP_LEN; i++) {
            System.arraycopy(srcMap[i], 0, tmpMap[i], 0, MAP_LEN);
        }

        //3 x 3 회전된 부분 배열
        rotate90(r, c, tmpMap);

        return tmpMap;
    }

    public static void rotate90(int r, int c, int[][] tmpMap) {
        int[][] partMap = new int[3][3];
        int[][] rotatedMap = new int[3][3];

        for (int i = 0; i < 3; i++) {
            System.arraycopy(tmpMap[r + i], c, partMap[i], 0, 3);
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                rotatedMap[i][j] = partMap[2 - j][i];
            }
        }

        for (int i = r, tr = 0; i < r + 3; i++, tr++) {
            for (int j = c, tc = 0; j < c + 3; j++, tc++) {
                tmpMap[i][j] = rotatedMap[tr][tc];
            }

        }
    }

    //1차 유물 획득 가치 확인
    //flag = 0 -> 획득 가능한 가치만 return | flag = 1 -> 실제 유물 제
    public static int calcScore(int[][] tmpMap, int flag) {
        int cnt = 1;
        int[] nums = new int[26];
        int[][] numMap = new int[MAP_LEN][MAP_LEN];
        boolean[][] visited = new boolean[MAP_LEN][MAP_LEN];

        for (int i = 0; i < MAP_LEN; i++) {
            for (int j = 0; j < MAP_LEN; j++) {
                if (!visited[i][j]) {
                    bfs(i, j, cnt, tmpMap, nums, numMap, visited);
                    ++cnt;
                }
            }
        }

        if (flag == 1) {
            for (int i = 0; i < MAP_LEN; i++) {
                for (int j = 0; j < MAP_LEN; j++) {
                    if (nums[numMap[i][j]] >= 3) tmpMap[i][j] = -1;
                }
            }
        }

        int willSaveScore = 0;
        for (int i = 1; i <= 25; i++) {
            if (nums[i] >= 3) willSaveScore += nums[i];
        }
        return willSaveScore;
    }

    public static void bfs(int r, int c, int cnt, int[][] tmpMap, int[] nums, int[][] numMap, boolean[][] visited) {
        Deque<Point> q = new ArrayDeque<>();
        q.add(new Point(r, c, tmpMap[r][c]));
        visited[r][c] = true;

        while (!q.isEmpty()) {
            Point p = q.poll();
            numMap[p.r][p.c] = cnt;
            nums[cnt]++;

            for (int i = 0; i < 4; i++) {
                int tr = p.r + d[i][0];
                int tc = p.c + d[i][1];

                if (invalidRange(tr, tc) || visited[tr][tc] || tmpMap[tr][tc] != p.n) continue;
                visited[tr][tc] = true;
                q.add(new Point(tr, tc, p.n));
            }
        }
    }

    public static boolean invalidRange(int r, int c) {
        return r < 0 || r >= MAP_LEN || c < 0 || c >= MAP_LEN;
    }
}

class RotateInfo implements Comparable<RotateInfo> {
    int score;
    int angle;
    int r;
    int c;
    int[][] map;

    public RotateInfo(int score, int angle, int r, int c, int[][] map) {
        this.score = score;
        this.angle = angle;
        this.r = r;
        this.c = c;
        this.map = map;
    }

    @Override
    public int compareTo(RotateInfo t) {
        if (this.score == t.score) {
            if (this.angle == t.angle) {
                if (this.c == t.c) {
                    return Integer.compare(this.r, t.r);
                }
                return Integer.compare(this.c, t.c);
            }
            return Integer.compare(this.angle, t.angle);
        }
        return Integer.compare(t.score, this.score);
    }
}

class Point {
    int r;
    int c;
    int n;

    public Point(int r, int c, int n) {
        this.r = r;
        this.c = c;
        this.n = n;
    }
}