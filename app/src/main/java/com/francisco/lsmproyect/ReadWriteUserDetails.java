package com.francisco.lsmproyect;

public class ReadWriteUserDetails {
    public String username, doB, mobile, location, bio;
    public int currentLevel, nextLevel, coursesCompleted, streak, totalExperience, levelProgress;

    public ReadWriteUserDetails(String textUsername, String textDoB, String textMobile, String textLocation, String textBio,
                                int currentLevel, int nextLevel, int coursesCompleted, int streak, int totalExperience,
                                int levelProgress) {
        this.username = textUsername;
        this.doB = textDoB;
        this.mobile = textMobile;
        this.location = textLocation;
        this.bio = textBio;
        this.currentLevel = currentLevel;
        this.nextLevel = nextLevel;
        this.coursesCompleted = coursesCompleted;
        this.streak = streak;
        this.totalExperience = totalExperience;
        this.levelProgress = levelProgress;
    }

    public ReadWriteUserDetails(String textUsername, String textDoB, String textMobile, String textLocation, String textBio) {
        this.username = textUsername;
        this.doB = textDoB;
        this.mobile = textMobile;
        this.location = textLocation;
        this.bio = textBio;
    }

    public ReadWriteUserDetails() {}
}
