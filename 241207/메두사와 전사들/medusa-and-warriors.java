import java.util.*;
import java.io.*;

public class Main {

    static int N;
    static int M;
    static Medusa medusa;
    static Pos parkPos;
    // static List<Warrior> warriorList;
    static int[][] map;
    static int[][] warriorMap;
    static int[][] d = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    static int[] answer;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        StringBuilder sb = new StringBuilder();
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        map = new int[N][N];
        warriorMap = new int[N][N];
        answer = new int[3];

        st = new StringTokenizer(br.readLine());
        medusa = new Medusa(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), -1);
        parkPos = new Pos(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));

        // warriorList = new ArrayList<>();
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < M; i++) {
            int r = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            warriorMap[r][c]++;
            // warriorList.add(new Warrior(r, c));
        }

        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        if (getMinimumParkDistance(medusa.getR(), medusa.getC()) == Integer.MAX_VALUE) {
            System.out.println(-1);
            return;
        }

        while (true) {
            answer = new int[3];

            //1. 메두사의 이동
            moveMedusa();

            //메두사 공원 도착 확인
            if (isParkPos(medusa.pos)) {
                sb.append(0);
                break;
            }

            //2. 메두사의 시선
            makeWarriorToStone();

            // System.out.println(medusa.getR() + ", " + medusa.getC());
            // for (int i = 0; i < N; i++) {
            //     System.out.println(Arrays.toString(medusa.visionMap[i]));
            // }System.out.println();

            //3. 전사들의 이동
            moveWarriories();

            sb.append(answer[0]).append(' ').append(answer[1]).append(' ').append(answer[2]).append('\n');
        }

        // for (int i = 0; i < N; i++) {
        //     System.out.println(Arrays.toString(warriorMap[i]));
        // }System.out.println();
        System.out.println(sb);
    }

    public static void moveWarriories() {
        int[][] newWarriorMap = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (warriorMap[i][j] > 0 && medusa.getVisionState(i, j) != 2) {
                    moveWarrior(i, j, newWarriorMap);
                } else if (warriorMap[i][j] > 0 && medusa.getVisionState(i, j) == 2) {
                    newWarriorMap[i][j] += warriorMap[i][j];
                }
            }
        }
        warriorMap = newWarriorMap;
    }

    public static void moveWarrior(int r, int c, int[][] newWarriorMap) {
        PriorityQueue<MoveInfo> pq = new PriorityQueue<>();
        int curDistance = medusa.getDistance(r, c);

        //첫 번째 이동
        for (int i = 0; i < 4; i++) {
            int tr = r + d[i][0];
            int tc = c + d[i][1];

            if (invalidRange(tr, tc) || medusa.getVisionState(tr, tc) == 1 || medusa.getVisionState(tr, tc) == 2) continue;

            int distance = medusa.getDistance(tr, tc);
            if (distance >= curDistance) continue;
            pq.add(new MoveInfo(distance, i));
        }
        if (!pq.isEmpty()) {
            MoveInfo moveInfo = pq.poll();
            int tr = r + d[moveInfo.dir][0];
            int tc = c + d[moveInfo.dir][1];
            answer[0] += warriorMap[r][c];
            if (medusa.isSamePos(tr, tc)) {
                answer[2] += warriorMap[r][c];
                return;
            }
            newWarriorMap[tr][tc] += warriorMap[r][c];
            r = tr;
            c = tc;
        } else newWarriorMap[r][c] += warriorMap[r][c];
        pq.clear();

        curDistance = medusa.getDistance(r, c);
        //두 번째 이동
        for (int i = 2; i < 6; i++) {
            int tr = r + d[i % 4][0];
            int tc = c + d[i % 4][1];

            if (invalidRange(tr, tc) || medusa.getVisionState(tr, tc) == 1 || medusa.getVisionState(tr, tc) == 2) continue;

            int distance = medusa.getDistance(tr, tc);
            if (distance >= curDistance) continue;
            pq.add(new MoveInfo(distance, i));
        }

        if (!pq.isEmpty()) {
            MoveInfo moveInfo = pq.poll();
            int tr = r + d[moveInfo.dir][0];
            int tc = c + d[moveInfo.dir][1];
            answer[0] += newWarriorMap[r][c];
            if (medusa.isSamePos(tr, tc)) {
                answer[2] += newWarriorMap[r][c];
                newWarriorMap[r][c] = 0;
                return;
            }
            newWarriorMap[tr][tc] += newWarriorMap[r][c];
            newWarriorMap[r][c] = 0;
        }
    }

    public static void makeWarriorToStone() {
        PriorityQueue<VisionInfo> pq = new PriorityQueue<>();
        for (int i = 0; i < 4; i++) {
            int[][] visionMap = makeVisionMap(i);
            int stonedCount = getStonedWarrior(visionMap, i);
            pq.add(new VisionInfo(visionMap, stonedCount, i));
        }
        VisionInfo visionInfo = pq.poll();
        medusa.setVisionMap(visionInfo.visionMap);
        answer[1] = visionInfo.stonedCount;

        // for (int i = 0; i < N; i++) {
        //     System.out.println(Arrays.toString(visionInfo.visionMap[i]));
        // }System.out.println(visionInfo.stonedCount);
    }

    public static int getStonedWarrior(int[][] visionMap, int dir) {
        int stonedCount = 0;
        int sideDir = 0;

        if (dir <= 1) {
            for (int i = medusa.getR(); 0 <= i && i < N ; i += d[dir][0]) {
                for (int j = 0; j < N; j++) {
                    if (visionMap[i][j] == 1 && warriorMap[i][j] > 0) {
                        int depth = 1;
                        stonedCount++;
                        visionMap[i][j] = 2;
                        if (medusa.getC() == j) {
                            while (true) {
                                int tr = i + (depth * d[dir][0]);
                                if (invalidRange(tr, j)) break;
                                visionMap[tr][j] = 3;
                                depth++;
                            }
                        } else {
                            if (medusa.getC() > j) sideDir = -1;
                            else sideDir = 1;
                            while (true) {
                                int tr = i + (depth * d[dir][0]);
                                if (invalidRange(tr, j)) break;
                                for (int k = 0; k <= depth; k++) {
                                    int tc = j + (sideDir * k);
                                    if (invalidRange(tr, tc)) continue;
                                    visionMap[tr][tc] = 3;
                                }
                                depth++;
                            }
                        }
                    }
                }
            }
        } else if (dir <= 3) {
            for (int i = 0; i < N; i++) {
                for (int j = medusa.getC(); 0 <= j && j < N; j += d[dir][1]) {
                    if (visionMap[i][j] == 1 && warriorMap[i][j] > 0) {
                        int depth = 1;
                        stonedCount++;
                        visionMap[i][j] = 2;
                        if (medusa.getR() == i) {
                            while (true) {
                                int tc = j + (depth * d[dir][1]);
                                if (invalidRange(i, tc)) break;
                                visionMap[i][tc] = 3;
                                depth++;
                            }
                        } else {
                            if (medusa.getR() > i) sideDir = -1;
                            else sideDir = 1;
                            while (true) {
                                int tc = j + (depth * d[dir][1]);
                                if (invalidRange(i, tc)) break;
                                for (int k = 0; k <= depth; k++) {
                                    int tr = i + (sideDir * k);
                                    if (invalidRange(tr, tc)) continue;
                                    visionMap[tr][tc] = 3;
                                }
                                depth++;
                            }
                        }
                    }
                }
            }
        }
        return stonedCount;
    }

    public static int[][] makeVisionMap(int dDir) {
        int[][] visionMap = new int[N][N];
        int depth = 1;

        if (dDir <= 1) {
            while (true) {
                int tr = medusa.getR() + (depth * d[dDir][0]);
                if (invalidRange(tr, medusa.getC())) return visionMap;

                for (int i = depth * -1; i <= depth; i++) {
                    int tc = medusa.getC() + i;
                    if (invalidRange(tr, tc)) continue;
                    visionMap[tr][tc] = 1;
                }
                ++depth;
            }
        } else if (dDir <= 3) {
            while (true) {
                int tc = medusa.getC() + (depth * d[dDir][1]);
                if (invalidRange(medusa.getR(), tc)) return visionMap;

                for (int i = depth * -1; i <= depth; i++) {
                    int tr = medusa.getR() + i;
                    if (invalidRange(tr, tc)) continue;
                    visionMap[tr][tc] = 1;
                }
                ++depth;
            }
        }
        return null;
    }

    public static void moveMedusa() {
        PriorityQueue<MoveInfo> pq = new PriorityQueue<>();
        int curDistance = getMinimumParkDistance(medusa.getR(), medusa.getC());

        for (int i = 0; i < 4; i++) {
            int tr = medusa.getR() + d[i][0];
            int tc = medusa.getC() + d[i][1];
            if (invalidRange(tr, tc) || map[tr][tc] == 1) continue;

            int distance = getMinimumParkDistance(tr, tc);
            // System.out.println(curDistance + " | " + distance);
            if (distance > curDistance) continue;
            pq.add(new MoveInfo(distance, i));
        }

        MoveInfo moveInfo = pq.poll();
        medusa.move(d[moveInfo.dir][0], d[moveInfo.dir][1]);

        if (warriorMap[medusa.getR()][medusa.getC()] > 0) {
            warriorMap[medusa.getR()][medusa.getC()] = 0;
        }
    }

    // public static int getMinimumParkDistance(int r, int c) {
    //     return Math.abs(parkPos.r - r) + Math.abs(parkPos.c - c);
    // }

    public static int getMinimumParkDistance(int r, int c) {
        Deque<Pos> q = new ArrayDeque<>();
        boolean[][] visited = new boolean[N][N];
        q.add(new Pos(r, c));
        visited[r][c] = true;

        while (!q.isEmpty()) {
            Pos pos = q.poll();
            if (isParkPos(pos)) return pos.distance;

            for (int i = 0; i < 4; i++) {
                int tr = pos.r + d[i][0];
                int tc = pos.c + d[i][1];

                if (invalidRange(tr, tc) || visited[tr][tc] || map[tr][tc] == 1) continue;
                Pos tmp = new Pos(tr, tc, pos.distance + 1);

                if (isParkPos(tmp)) return tmp.distance;
                q.add(tmp);
                visited[tr][tc] = true;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static boolean isParkPos(Pos pos) {
        return pos.r == parkPos.r && pos.c == parkPos.c;
    }

    public static boolean invalidRange(int r, int c) {
        return r < 0 || r >= N || c < 0 || c >= N;
    }
}

class Medusa {
    Pos pos;
    int dir;
    int[][] visionMap;

    public Medusa(int r, int c, int dir) {
        this.pos = new Pos(r, c);
        this.dir = dir;
    }

    public void move(int dr, int dc) {
        this.pos = new Pos(this.getR() + dr, this.getC() + dc);
    }

    public int getDistance(int r, int c) {
        return Math.abs(this.getR() - r) + Math.abs(this.getC() - c);
    }

    public int getVisionState(int r, int c) {
        return visionMap[r][c];
    }

    public int getR() {
        return this.pos.r;
    }

    public int getC() {
        return this.pos.c;
    }

    public boolean isSamePos(int r, int c) {
        return this.getR() == r && this.getC() == c;
    }

    public void setVisionMap(int[][] visionMap) {
        this.visionMap = visionMap;
    }
}

class Pos {
    int r;
    int c;
    int distance;

    public Pos(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public Pos(int r, int c, int distance) {
        this.r = r;
        this.c = c;
        this.distance = distance;
    }
}

class MoveInfo implements Comparable<MoveInfo> {
    int distance;
    int dir;

    public MoveInfo(int distance, int dir) {
        this.distance = distance;
        this.dir = dir;
    }

    @Override
    public int compareTo(MoveInfo o) {
        if (Integer.compare(this.distance, o.distance) == 0) {
            return Integer.compare(this.dir, o.dir);
        }
        return Integer.compare(this.distance, o.distance);
    }
}

class VisionInfo implements Comparable<VisionInfo> {
    int[][] visionMap;
    int stonedCount;
    int dir;

    public VisionInfo(int[][] visionMap, int stonedCount, int dir) {
        this.visionMap = visionMap;
        this.stonedCount = stonedCount;
        this.dir = dir;
    }

    @Override
    public int compareTo(VisionInfo o) {
        if (Integer.compare(this.stonedCount, o.stonedCount) == 0) {
            return Integer.compare(this.dir, o.dir);
        }
        return Integer.compare(o.stonedCount, this.stonedCount);
    }
}