package diablo;

import java.lang.Math;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.Writer;

/**
 * This class gives an estimated damage increase going from X% AD to Y% AD.
 *
 * Note:
 * - Skill & Target radius is assumed to be fixed.
 * - Probabilities are calculated using by uniformly random simulations.
 * - This assumes uniformly
 * distributed monsters.
 * - Based on data from DATA_FILE located in LOCAL_DIRECTORY
 *
 * @version 1.0.0
 * @author VocaloidNyan
 */
public class Diablo {

    final static int SAMPLE_SIZE = 100000;
    final static double AD_chance = 0.2; //Chance to apply area damage
    final static double AD_radius = 10.0; //Size of the AD proc
    final static String BACK_UP_FILE = "backup.txt";
    final static String INPUT_FILE = "Hit vs Ring.txt"; //Seperated by spaces
    final static String DATA_FILE = "total.txt"; //Seperated by tabs
    final static String LOCAL_DIRECTORY = "C:\\Users\\VocaloidNyan\\Documents\\NetBeansProjects\\Diablo\\src\\diablo\\";
    final static int MAX_MONSTER_NUM = 11;

    /**
     *
     * @param args - No arguments from cmd line used in this case
     */
    public static void main(String[] args) {
        final double old_AD = 0.5;
        final double new_AD = 0.7;
        final double skill_radius = 10.0;
        final double target_radius = 1.0;
        System.out.println("(>^^)>");
        System.out.println("Sample Size: " + SAMPLE_SIZE);
        System.out.println("AD chance: " + AD_chance);
        System.out.println("Current AD: " + old_AD * 100.0 + "%");
        System.out.println("New AD: " + new_AD * 100.0 + "%");
        System.out.println("Skill radius: " + skill_radius + " yards");
        System.out.println("Target radius: " + target_radius + " yards");
        System.out.println("Data file: " + LOCAL_DIRECTORY + DATA_FILE + "\n");
        double e_dmg_incr;
        Diablo d = new Diablo();
        try {
            //d.prob_coefficent(SAMPLE_SIZE, 2, 5, 10.0, 0.0);
            e_dmg_incr = d.AD_expected_increase(d.get_data(), skill_radius,
                    target_radius, old_AD, new_AD);
            System.out.println("Expected damage increase : " + 100.0 * (e_dmg_incr - 1.0) + "%");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method calculates the AD coefficent.
     * 
     * @param sample - Number of trials for generating uniformly random points
     * @param monsters_hit - Number of additional monsters hit from skill (AoE)
     * @param monsters_ring - Number of monsters in the 10 yard radius ring,
     * around the skill circle
     * @param skill_radius - Size of the skill (AoE) in yards
     * @param target_radius - Size of the target in yards
     * @return Returns the AD coefficent
     */
    public double AD_coefficent(int sample, int monsters_hit, int monsters_ring,
            double skill_radius, double target_radius) {
        int num_skill_AD_hit = (int) monsters_hit * (monsters_hit - 1) / 2; // Number of possible lines for point in skill to point in skill
        int num_ring_AD_hit = (int) monsters_hit * monsters_ring; // Number of possible lines for point in skill to point in ring
        int count; // Used for counters
        double p_coeff = (double) (2.0 * monsters_hit);
        int[] yes_skill = new int[num_skill_AD_hit]; // Keeps track the number of skill to skill lines less than 10 yards
        int[] yes_ring = new int[num_ring_AD_hit]; // Keeps track the number of skill to ring lines less than 10 yards
        double[] distance_skill = new double[num_skill_AD_hit]; // Distance of connecting lines (point skill to skill)
        double[] distance_ring = new double[num_ring_AD_hit]; // Distance of connecting lines (point skill to ring)
        for (int i = 0; i < sample; i++) {
            count = 0;
            double[][] r_s = new double[monsters_hit][2]; // Random numbers for skill to skill lines
            double[][] r_r = new double[monsters_ring][2]; // Random numbers for skill to ring lines
            /* Point skill to skill calculations & computations */
            for (int j = 0; j < monsters_hit; j++) {
                r_s[j][0] = Math.random() * (skill_radius * skill_radius
                        - target_radius * target_radius) + target_radius * target_radius; // r
                r_s[j][1] = Math.random(); // theta
            }
            for (int j = 0; j < monsters_hit - 1; j++) {
                for (int k = j + 1; k < monsters_hit; k++) {
                    distance_skill[count] = Math.sqrt(r_s[j][0] + r_s[k][0] - 2 * Math.sqrt(r_s[j][0]
                            * r_s[k][0]) * Math.cos(2.0 * Math.PI * (r_s[j][1] - r_s[k][1]))); // Line distance
                    count++;
                }
            }
            count = 0;
            for (int j = 0; j < num_skill_AD_hit; j++) {
                if (distance_skill[j] < AD_radius) {
                    count++;
                }
            }
            if (count != 0) {
                yes_skill[count - 1]++; // Records number of lines with length < radius
            }
            count = 0;
            /* Skill to ring calculations & computations */
            for (int j = 0; j < monsters_ring; j++) {
                r_r[j][0] = Math.random() * (2 * AD_radius * skill_radius
                        + AD_radius * AD_radius) + skill_radius * skill_radius; // r
                r_r[j][1] = Math.random(); // theta
            }
            for (int j = 0; j < monsters_hit; j++) {
                for (int k = 0; k < monsters_ring; k++) {
                    distance_ring[count] = Math.sqrt(r_s[j][0] + r_r[k][0] - 2 * Math.sqrt(r_s[j][0]
                            * r_r[k][0]) * Math.cos(2 * Math.PI * (r_s[j][1] - r_r[k][1]))); // Line distance (skill to ring)
                    count++;
                }
            }
            count = 0;
            for (int j = 0; j < num_ring_AD_hit; j++) // Counts number of lines with length < radius
            {
                if (distance_ring[j] < AD_radius) {
                    count++;
                }
            }
            if (count != 0) {
                yes_ring[count - 1]++; // Records number of lines with length < radius
            }
        }
        for (int i = 1; i <= num_skill_AD_hit; i++) {
            p_coeff += 2.0 * (double) i * yes_skill[i - 1] / sample;
        }
        for (int i = 1; i <= num_ring_AD_hit; i++) {
            p_coeff += (double) i * yes_ring[i - 1] / sample;
        }
        return p_coeff;
    }

    /**
     * This method calculates the expected damage increase on the respective AD.
     * 
     * @param freq_chart - A chart with the frequencies of # of monsters in
     * skill radius and # of monsters in 10 yard radius outer ring.
     * @param skill_radius - Size of the skill (AoE) in yards
     * @param target_radius - Size of the target in yards
     * @param old_AD - The current AD%
     * @param new_AD - The new AD%, which we are comparing to
     * @return The expected damage% increase going from old_AD to new_AD
     */
    public double AD_expected_increase(int[][] freq_chart, double skill_radius,
            double target_radius, double old_AD, double new_AD) {
        Diablo d = new Diablo();
        double old_edmg = 0.0, new_edmg = 0.0;
        double AD_coeff;
        for (int i = 0; i < MAX_MONSTER_NUM; i++) {
            for (int j = 0; j < MAX_MONSTER_NUM; j++) {
                if (freq_chart[i][j] != 0) {
                    AD_coeff = d.AD_coefficent(SAMPLE_SIZE, i, j, skill_radius, target_radius);
                    old_edmg += freq_chart[i][j] * (i + 1.0 + AD_chance * old_AD * AD_coeff);
                    new_edmg += freq_chart[i][j] * (i + 1.0 + AD_chance * new_AD * AD_coeff);
                }
            }
        }
        return new_edmg / old_edmg;
    }

    /**
     * This method returns the frequency chart which was gathered from reading
     * the data files (total.txt, Hit vs Ring.txt).
     * 
     * @return Returns the frequency chart
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public int[][] get_data() throws IOException, FileNotFoundException {
        int[][] freq_data = new int[MAX_MONSTER_NUM][MAX_MONSTER_NUM];
        String[] str;
        File data_file = new File(LOCAL_DIRECTORY + DATA_FILE);
        File input_file = new File(LOCAL_DIRECTORY + INPUT_FILE);
        String line;
        StringBuilder c = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            BufferedReader input = new BufferedReader(new FileReader(data_file));
            try {
                line = null;
                while ((line = input.readLine()) != null) {
                    c.append(line);
                    c.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        str = c.toString().trim().split("\t");
        for (int i = 0; i < MAX_MONSTER_NUM; i++) {
            for (int j = 0; j < MAX_MONSTER_NUM; j++) {
                freq_data[i][j] = Integer.parseInt(str[j + MAX_MONSTER_NUM * i].trim());
            }
        }
        c = new StringBuilder();
        System.out.println("Press \"y\" to add data from " + INPUT_FILE + " into "
                + DATA_FILE + ".\nAny other key would use current data in " + DATA_FILE + ".");
        try {
            BufferedReader input = new BufferedReader(new FileReader(input_file));
            if (br.readLine().compareTo("y") == 0) {
                System.out.println("Data pool added");
                try {
                    line = null;
                    while ((line = input.readLine()) != null) {
                        c.append(line);
                        c.append(System.getProperty("line.separator"));
                    }
                } finally {
                    input.close();
                }
                str = c.toString().trim().split(" ");
                line = "";
                for (int i = 0; 2 * i < str.length; i++) {
                    freq_data[Integer.parseInt(str[2 * i].trim())][Integer.parseInt(str[2 * i + 1].trim())]++;
                }
                for (int i = 0; i < MAX_MONSTER_NUM; i++) {
                    for (int j = 0; j < MAX_MONSTER_NUM; j++) {
                        line = line.concat(Integer.toString(freq_data[i][j]));
                        line = line.concat("\t");
                    }
                    line = line.concat("\n");
                }
                if (data_file == null) {
                    throw new IllegalArgumentException("File should not be null.");
                }
                if (!data_file.exists()) {
                    throw new FileNotFoundException("File does not exist: " + data_file);
                }
                if (!data_file.isFile()) {
                    throw new IllegalArgumentException("Should not be a directory: " + data_file);
                }
                if (!data_file.canWrite()) {
                    throw new IllegalArgumentException("File cannot be written: " + data_file);
                }
                Writer output = new BufferedWriter(new FileWriter(data_file));
                try {
                    output.write(line);
                } finally {
                    output.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return freq_data;
    }
}
