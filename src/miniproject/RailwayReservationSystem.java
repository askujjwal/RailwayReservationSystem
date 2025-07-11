package miniproject;

import java.util.*;
import java.io.*;

class Train {
    int trainNo;
    String name, source, destination;
    List<List<Boolean>> bookedSeats;
    static final int COACHES = 7;
    static final int SEATS_PER_COACH = 100;

    public Train(int trainNo, String name, String source, String destination) {
        this.trainNo = trainNo;
        this.name = name;
        this.source = source;
        this.destination = destination;
        bookedSeats = new ArrayList<>();
        for (int i = 0; i < COACHES; i++) {
            bookedSeats.add(new ArrayList<>(Collections.nCopies(SEATS_PER_COACH, false)));
        }
    }

    void display() {
        int total = 0;
        for (List<Boolean> seats : bookedSeats) {
            for (boolean booked : seats) {
                if (booked) total++;
            }
        }
        System.out.println(trainNo + " | " + name + " | " + source + " -> " + destination +
                " | Available: " + (COACHES * SEATS_PER_COACH - total));
    }

    int[] assignSeat(int age) {
        int start = age > 45 ? 0 : 50;
        int end = age > 45 ? 49 : 99;

        for (int coach = 0; coach < COACHES; coach++) {
            List<Boolean> seats = bookedSeats.get(coach);
            for (int seat = start; seat <= end; seat++) {
                if (!seats.get(seat)) {
                    seats.set(seat, true);
                    return new int[]{coach + 1, seat + 1}; // 1-based for user
                }
            }
        }
        return null;
    }

    void cancelSeat(int coach, int seat) {
        bookedSeats.get(coach - 1).set(seat - 1, false); // convert to 0-based
    }
}

class Ticket {
    static int idCounter = 1000;
    int id, age, trainNo, coach, seat;
    String name;

    public Ticket(String name, int age, int trainNo, int coach, int seat) {
        this.id = idCounter++;
        this.name = name;
        this.age = age;
        this.trainNo = trainNo;
        this.coach = coach;
        this.seat = seat;
    }

    void display() {
        System.out.println("Ticket ID: " + id + " | Name: " + name + " | Age: " + age +
                " | Train: " + trainNo + " | Coach: " + coach + " | Seat: " + seat);
    }

    String toCSV() {
        return id + "," + name + "," + age + "," + trainNo + "," + coach + "," + seat;
    }

    static Ticket fromCSV(String line) {
        String[] parts = line.split(",");
        Ticket t = new Ticket(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                              Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
        t.id = Integer.parseInt(parts[0]);
        if (t.id >= idCounter) idCounter = t.id + 1;
        return t;
    }
}

public class RailwayReservationSystem {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Train> trains = new ArrayList<>();
    static List<Ticket> tickets = new ArrayList<>();
    static final String FILE_NAME = "tickets.csv";

    public static void main(String[] args) {
        addTrains();
        loadTickets();

        while (true) {
            try {
                System.out.println("\n--- Railway Reservation System ---");
                System.out.println("1. View Trains");
                System.out.println("2. Book Ticket");
                System.out.println("3. Cancel Ticket");
                System.out.println("4. View Tickets");
                System.out.println("5. Exit");
                System.out.print("Choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> viewTrains();
                    case 2 -> bookTicket();
                    case 3 -> cancelTicket();
                    case 4 -> viewTickets();
                    case 5 -> {
                        saveTickets();
                        System.out.println("Thank you!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                sc.nextLine(); // clear buffer
            }
        }
    }

    static void addTrains() {
        trains.add(new Train(101, "Shatabdi", "Delhi", "Bhopal"));
        trains.add(new Train(102, "Rajdhani", "Mumbai", "Delhi"));
        trains.add(new Train(103, "Duronto", "Chennai", "Kolkata"));
    }

    static void viewTrains() {
        for (Train t : trains) {
            t.display();
        }
    }

    // Helper method for name input
    static String getValidName() {
        while (true) {
            System.out.print("Enter Name: ");
            String input = sc.nextLine().trim();
            if (input.matches("[a-zA-Z ]+")) {
                return input;
            } else {
                System.out.println("Invalid name. Please enter only alphabets and spaces.");
            }
        }
    }

    // Helper method to find a train by its number
    static Train findTrainByNumber(int trainNo) {
        for (Train train : trains) {
            if (train.trainNo == trainNo) {
                return train;
            }
        }
        return null;
    }

    static void bookTicket() {
        sc.nextLine(); // clear buffer
        String name = getValidName();
        int age = 0;
        int trainNo;

        // Age input: must be integer between 1 and 110
        while (true) {
            System.out.print("Enter Age (1-110): ");
            String ageInput = sc.nextLine().trim();
            try {
                age = Integer.parseInt(ageInput);
                if (age >= 1 && age <= 110) {
                    break;
                } else {
                    System.out.println("Age must be between 1 and 110.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid age. Please enter a valid number.");
            }
        }

        // Train number input
        Train train = null;
        while (true) {
            System.out.print("Enter Train Number: ");
            String trainInput = sc.nextLine().trim();
            try {
                trainNo = Integer.parseInt(trainInput);
                train = findTrainByNumber(trainNo);
                if (train != null) {
                    break;
                } else {
                    System.out.println("Train not found. Please enter a valid train number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid train number.");
            }
        }

        int[] seatInfo = train.assignSeat(age);
        if (seatInfo == null) {
            System.out.println("No seat available as per your berth preference.");
        } else {
            Ticket ticket = new Ticket(name, age, trainNo, seatInfo[0], seatInfo[1]);
            tickets.add(ticket);
            saveTickets(); // Save immediately after booking
            System.out.println("Ticket Booked! Ticket ID: " + ticket.id);
        }
    }

    static void cancelTicket() {
        try {
            System.out.print("Enter Ticket ID to cancel: ");
            int id = sc.nextInt();
            Ticket toCancel = null;

            for (Ticket t : tickets) {
                if (t.id == id) {
                    toCancel = t;
                    break;
                }
            }

            if (toCancel != null) {
                Train train = findTrainByNumber(toCancel.trainNo);
                if (train != null) {
                    train.cancelSeat(toCancel.coach, toCancel.seat);
                }
                tickets.remove(toCancel);
                saveTickets(); // Save immediately after cancellation
                System.out.println("Ticket cancelled successfully.");
            } else {
                System.out.println("Ticket not found.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
            sc.nextLine();
        }
    }

    static void viewTickets() {
        if (tickets.isEmpty()) {
            System.out.println("No tickets booked.");
        } else {
            for (Ticket t : tickets) {
                t.display();
            }
        }
    }

    // Updated: Write header row before ticket entries
    static void saveTickets() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println("ID,Name,Age,TrainNo,Coach,Seat"); // header row
            for (Ticket t : tickets) {
                writer.println(t.toCSV());
            }
        } catch (IOException e) {
            System.out.println("Error saving tickets: " + e.getMessage());
        }
    }

    static void loadTickets() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { // skip header
                    firstLine = false;
                    continue;
                }
                Ticket t = Ticket.fromCSV(line);
                tickets.add(t);
                // Mark seat as booked in the Train's bookedSeats
                Train train = findTrainByNumber(t.trainNo);
                if (train != null) {
                    train.bookedSeats.get(t.coach - 1).set(t.seat - 1, true);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading tickets: " + e.getMessage());
        }
    }
}
