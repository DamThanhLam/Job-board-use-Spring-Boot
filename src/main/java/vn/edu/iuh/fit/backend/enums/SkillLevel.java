package vn.edu.iuh.fit.backend.enums;

public enum SkillLevel {
    MASTER(1),
    BEGINER(2),
    ADVANCED(3),
    PROFESSIONAL(4),
    IMTERMEDIATE(5);
    private int level;

    SkillLevel(int i) {
        level = i;
    }

    public int getLevel() {
        return level;
    }
}
