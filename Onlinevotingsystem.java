import java.util.*;

// ================================================================
//  ONLINE VOTING SYSTEM
//  No Database — Pure In-Memory (HashMap / ArrayList)
//  College Mini Project | OOP in Java
//
//  OOP Concepts Used:
//  1. Encapsulation  → Voter, Candidate, Vote classes
//  2. Abstraction    → abstract class Election
//  3. Inheritance    → GeneralElection extends Election
//  4. Polymorphism   → displayResults() overridden
//  5. Interface      → Manageable implemented by Admin
// ================================================================


// ── INTERFACE 
interface Manageable {
    void add();
    void remove(int id);
    void viewAll();
}


//  1. Voter Class  — ENCAPSULATION

class Voter {

    private String  voterId;
    private String  name;
    private String  email;
    private String  password;
    private boolean hasVoted;
    private int     votedFor;   

    // Constructor
    public Voter(String voterId, String name, String email, String password) {
        this.voterId   = voterId;
        this.name      = name;
        this.email     = email;
        this.password  = password;
        this.hasVoted  = false;
        this.votedFor  = -1;
    }


    public String  getVoterId() { return voterId; }
    public String  getName()    { return name; }
    public String  getEmail()   { return email; }
    public boolean hasVoted()   { return hasVoted; }
    public int     getVotedFor(){ return votedFor; }

    
    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

  
    public void markVoted(int candidateId) {
        this.hasVoted = true;
        this.votedFor = candidateId;
    }

    @Override
    public String toString() {
        return String.format("Voter[%-5s | %-15s | %-22s | Voted: %s]",
            voterId, name, email, hasVoted ? "Yes" : "No");
    }
}


//  2. Candidate Class  — ENCAPSULATION

class Candidate {

    private int    candidateId;
    private String name;
    private String party;
    private String motto;
    private int    voteCount;

    public Candidate(int candidateId, String name, String party, String motto) {
        this.candidateId = candidateId;
        this.name        = name;
        this.party       = party;
        this.motto       = motto;
        this.voteCount   = 0;
    }

    public int    getCandidateId() { return candidateId; }
    public String getName()        { return name; }
    public String getParty()       { return party; }
    public String getMotto()       { return motto; }
    public int    getVoteCount()   { return voteCount; }


    public void incrementVote() {
        this.voteCount++;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-20s | %-16s | \"%s\" | Votes: %d",
            candidateId, name, party, motto, voteCount);
    }
}


//  3. Vote Class  — stores each vote record

class Vote {

    private String voterId;
    private int    candidateId;
    private long   timestamp;

    public Vote(String voterId, int candidateId) {
        this.voterId     = voterId;
        this.candidateId = candidateId;
        this.timestamp   = System.currentTimeMillis();
    }

    public String getVoterId()   { return voterId; }
    public int    getCandidateId(){ return candidateId; }
    public long   getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Vote[VoterID=" + voterId + " → CandidateID=" + candidateId + "]";
    }
}

//  4. Abstract Election Class  

abstract class Election {

    protected String          electionName;
    protected List<Candidate> candidates;
    protected List<Vote>      votes;

    public Election(String electionName) {
        this.electionName = electionName;
        this.candidates   = new ArrayList<>();
        this.votes        = new ArrayList<>();
    }

    // ── Abstract method: subclass MUST override this ──
    public abstract void displayResults();

    // ── Concrete shared methods ──
    public void addCandidate(Candidate c) {
        candidates.add(c);
        System.out.println("[Election] Candidate added: " + c.getName()
            + " (" + c.getParty() + ")");
    }

    public boolean removeCandidate(int candidateId) {
        // Can only remove if no votes received
        Candidate target = findCandidate(candidateId);
        if (target == null) {
            System.out.println("[Election] Candidate #" + candidateId + " not found.");
            return false;
        }
        if (target.getVoteCount() > 0) {
            System.out.println("[Election] Cannot remove " + target.getName()
                + " — already has " + target.getVoteCount() + " vote(s).");
            return false;
        }
        candidates.remove(target);
        System.out.println("[Election] Candidate " + target.getName() + " removed.");
        return true;
    }

    public void recordVote(Vote v) {
        votes.add(v);
    }

    public Candidate findCandidate(int id) {
        return candidates.stream()
            .filter(c -> c.getCandidateId() == id)
            .findFirst()
            .orElse(null);
    }

    public List<Candidate> getCandidates()   { return candidates; }
    public List<Vote>      getVotes()        { return votes; }
    public String          getElectionName() { return electionName; }
    public int             getTotalVotes()   { return votes.size(); }
}


//  5. GeneralElection  — INHERITANCE + POLYMORPHISM

class GeneralElection extends Election {

    public GeneralElection(String electionName) {
        super(electionName);   // calls Election constructor
    }

    // ── POLYMORPHISM: overrides abstract method ──
    @Override
    public void displayResults() {
        int total = votes.size();

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("   ELECTION RESULTS: " + electionName);
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.printf("%-5s %-20s %-16s %-8s %-6s%n",
            "ID", "Candidate", "Party", "Votes", "%");
        System.out.println("──────────────────────────────────────────────");

        // Sort by vote count descending
        candidates.stream()
            .sorted(Comparator.comparingInt(Candidate::getVoteCount).reversed())
            .forEach(c -> {
                double pct = total > 0 ? (c.getVoteCount() * 100.0 / total) : 0;
                System.out.printf("%-5d %-20s %-16s %-8d %.1f%%%n",
                    c.getCandidateId(), c.getName(),
                    c.getParty(), c.getVoteCount(), pct);
            });

        System.out.println("──────────────────────────────────────────────");
        System.out.println("Total Votes Cast : " + total);

        Candidate winner = getWinner();
        if (winner != null && winner.getVoteCount() > 0) {
            System.out.println("WINNER           : " + winner.getName()
                + " — " + winner.getParty());
        } else {
            System.out.println("No votes cast yet.");
        }
        System.out.println();
    }

    // ── Get candidate with highest votes ──
    public Candidate getWinner() {
        return candidates.stream()
            .max(Comparator.comparingInt(Candidate::getVoteCount))
            .orElse(null);
    }
}

//  6. Admin Class  — INTERFACE implementation

class Admin implements Manageable {

    private int     adminId;
    private String  username;

    // Admin holds references to shared data stores
    private List<Candidate>     candidateList;
    private Map<String, Voter>  voterMap;
    private Election            election;
    private int                 nextCandidateId;

    public Admin(int adminId, String username,
                 List<Candidate> candidateList,
                 Map<String, Voter> voterMap,
                 Election election,
                 int nextCandidateId) {
        this.adminId        = adminId;
        this.username       = username;
        this.candidateList  = candidateList;
        this.voterMap       = voterMap;
        this.election       = election;
        this.nextCandidateId = nextCandidateId;
    }

    public String getUsername()    { return username; }
    public int    getNextCandId()  { return nextCandidateId; }

    // ── Manageable.add() ── (generic — use addCandidate for specifics)
    @Override
    public void add() {
        System.out.println("[Admin] Use addCandidate(name, party, motto) for candidates.");
    }

    // ── Add Candidate (Admin privilege) ──
    public boolean addCandidate(String name, String party, String motto) {
        if (name == null || name.isEmpty() || party == null || party.isEmpty()) {
            System.out.println("[Admin] ERROR: Name and party are required.");
            return false;
        }
        Candidate c = new Candidate(nextCandidateId++, name, party, motto);
        election.addCandidate(c);
        return true;
    }

    // ── Manageable.remove() ── removes a candidate by ID
    @Override
    public void remove(int candidateId) {
        election.removeCandidate(candidateId);
    }

    // ── Manageable.viewAll() ── shows all candidates WITH vote counts (Admin only)
    @Override
    public void viewAll() {
        int total = election.getTotalVotes();
        System.out.println("\n[Admin] === CANDIDATE STANDINGS (Admin View) ===");
        System.out.printf("%-5s %-20s %-16s %-8s %-6s%n",
            "ID", "Name", "Party", "Votes", "%");
        System.out.println("──────────────────────────────────────────────");
        election.getCandidates().stream()
            .sorted(Comparator.comparingInt(Candidate::getVoteCount).reversed())
            .forEach(c -> {
                double pct = total > 0 ? (c.getVoteCount() * 100.0 / total) : 0;
                System.out.printf("%-5d %-20s %-16s %-8d %.1f%%%n",
                    c.getCandidateId(), c.getName(),
                    c.getParty(), c.getVoteCount(), pct);
            });
        System.out.println("Total Votes: " + total);
    }

    // ── Register new voter (Admin privilege) ──
    public String registerVoter(String voterId, String name, String email, String password) {
        if (voterMap.containsKey(voterId)) {
            System.out.println("[Admin] ERROR: Voter ID '" + voterId + "' already exists.");
            return "ERROR: Duplicate ID";
        }
        if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("[Admin] ERROR: Name and password are required.");
            return "ERROR: Missing fields";
        }
        Voter v = new Voter(voterId, name, email, password);
        voterMap.put(voterId, v);
        System.out.println("[Admin] Voter registered: " + name + " (" + voterId + ")");
        return "SUCCESS";
    }

    // ── View voter participation status (Admin only — NOT shown to voters) ──
    public void viewVoterStatus() {
        System.out.println("\n[Admin] === VOTER STATUS (Private — Admin Only) ===");
        System.out.printf("%-8s %-18s %-22s %-10s %-15s%n",
            "ID", "Name", "Email", "Status", "Voted For");
        System.out.println("──────────────────────────────────────────────────────");
        voterMap.forEach((id, v) -> {
            String votedFor = "—";
            if (v.hasVoted()) {
                Candidate c = election.findCandidate(v.getVotedFor());
                votedFor = c != null ? c.getName() : "#" + v.getVotedFor();
            }
            System.out.printf("%-8s %-18s %-22s %-10s %-15s%n",
                v.getVoterId(), v.getName(), v.getEmail(),
                v.hasVoted() ? "Voted" : "Pending", votedFor);
        });
    }

    // ── Remove voter (only if not yet voted) ──
    public boolean removeVoter(String voterId) {
        Voter v = voterMap.get(voterId);
        if (v == null) {
            System.out.println("[Admin] Voter '" + voterId + "' not found.");
            return false;
        }
        if (v.hasVoted()) {
            System.out.println("[Admin] Cannot remove " + v.getName()
                + " — already voted.");
            return false;
        }
        voterMap.remove(voterId);
        System.out.println("[Admin] Voter " + voterId + " (" + v.getName() + ") removed.");
        return true;
    }
}

//  7. VotingSystem Class  — Main Controller

class VotingSystem {

    private Map<String, Voter> voters;       // HashMap: voterId → Voter
    private Election           election;
    private boolean            isElectionOpen;

    public VotingSystem(Election election) {
        this.voters          = new HashMap<>();
        this.election        = election;
        this.isElectionOpen  = true;
    }

    // ── Register voter into in-memory map ──
    public void registerVoter(Voter voter) {
        if (voters.containsKey(voter.getVoterId())) {
            System.out.println("ERROR: Voter ID '" + voter.getVoterId() + "' already exists.");
            return;
        }
        voters.put(voter.getVoterId(), voter);
        System.out.println("Voter registered: " + voter.getName()
            + " [" + voter.getVoterId() + "]");
    }

    // ── Cast Vote — full validation chain ──
    public boolean castVote(String voterId, String password, int candidateId) {

        // Check 1: election open?
        if (!isElectionOpen) {
            System.out.println("[VOTE FAILED] Election is closed.");
            return false;
        }

        // Check 2: voter exists?
        Voter voter = voters.get(voterId);
        if (voter == null) {
            System.out.println("[VOTE FAILED] Voter ID '" + voterId + "' not found.");
            return false;
        }

        // Check 3: correct password? (Encapsulation — authenticate() used)
        if (!voter.authenticate(password)) {
            System.out.println("[VOTE FAILED] Wrong password for " + voter.getName() + ".");
            return false;
        }

        // Check 4: already voted?
        if (voter.hasVoted()) {
            System.out.println("[VOTE FAILED] " + voter.getName()
                + " has already voted!");
            return false;
        }

        // Check 5: candidate exists?
        Candidate target = election.findCandidate(candidateId);
        if (target == null) {
            System.out.println("[VOTE FAILED] Candidate #" + candidateId + " not found.");
            return false;
        }

        // ── All checks passed — record vote ──
        Vote vote = new Vote(voterId, candidateId);
        election.recordVote(vote);
        target.incrementVote();
        voter.markVoted(candidateId);

        System.out.println("[VOTE OK] " + voter.getName()
            + " voted for " + target.getName()
            + " (" + target.getParty() + ")");
        return true;
    }

    // ── Close election ──
    public void closeElection() {
        isElectionOpen = false;
        System.out.println("\n[SYSTEM] Election is now CLOSED. No more votes accepted.");
    }

    // ── Show results (Polymorphism: calls overridden displayResults) ──
    public void showResults() {
        election.displayResults();
    }

    // ── Getters ──
    public Map<String, Voter> getVoters()  { return voters; }
    public Election           getElection(){ return election; }
    public boolean            isOpen()     { return isElectionOpen; }
}


//  8. Main Class  — Entry Point

public class OnlineVotingSystem {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("       ONLINE VOTING SYSTEM — OOP in Java       ");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        // Create Election  (Abstraction — using abstract class)

        GeneralElection election = new GeneralElection(
            "College Student Council Election 2026");

        VotingSystem system = new VotingSystem(election);

        // Add Candidates directly (pre-seeded)

        System.out.println("── Adding Candidates ──");
        election.addCandidate(new Candidate(1, "Arjun Sharma",
            "Progress Party",  "Building a better tomorrow"));
        election.addCandidate(new Candidate(2, "Priya Mehta",
            "Unity Front",     "Together we rise"));
        election.addCandidate(new Candidate(3, "Rahul Verma",
            "Students First",  "Your voice, our mission"));


        //  Register Voters

        System.out.println("\n── Registering Voters ──");
        system.registerVoter(new Voter("V001", "Amit Kumar",
            "amit@college.edu",  "pass123"));
        system.registerVoter(new Voter("V002", "Sneha Gupta",
            "sneha@college.edu", "abc456"));
        system.registerVoter(new Voter("V003", "Rohan Das",
            "rohan@college.edu", "xyz789"));
        system.registerVoter(new Voter("V004", "Pooja Singh",
            "pooja@college.edu", "qwerty"));
        system.registerVoter(new Voter("V005", "Kiran Nair",
            "kiran@college.edu", "kiran99"));

        // Admin adds a new candidate + voter  (Interface)
      
        System.out.println("\n── Admin Actions ──");
        Admin admin = new Admin(
            1, "admin",
            election.getCandidates(),
            system.getVoters(),
            election,
            4   // nextCandidateId starts at 4
        );

        admin.addCandidate("Nisha Patel", "Youth Alliance", "Change starts with us");
        admin.registerVoter("V006", "Dev Roy", "dev@college.edu", "dev123");

        //  Cast Votes
 
        System.out.println("\n── Casting Votes ──");
        system.castVote("V001", "pass123", 1);
        system.castVote("V002", "abc456",  2);
        system.castVote("V003", "xyz789",  1);
        system.castVote("V004", "qwerty",  3);
        system.castVote("V005", "kiran99", 2);
        system.castVote("V006", "dev123",  4);

        // Test edge cases

        System.out.println("\n── Edge Case: Double Vote ──");
        system.castVote("V001", "pass123", 2);     // already voted → FAIL

        System.out.println("\n── Edge Case: Wrong Password ──");
        system.castVote("V002", "wrongpass", 1);   // bad password → FAIL

        System.out.println("\n── Edge Case: Unknown Voter ──");
        system.castVote("V999", "test", 1);        // voter not found → FAIL

        //  Admin views private data (not shown to voters)
 
        admin.viewVoterStatus();   // shows who voted for whom (admin only)
        admin.viewAll();           // shows vote counts (admin only)

        //  Admin removes unused candidate (0 votes)

        System.out.println("\n── Admin: Remove candidate with votes (should fail) ──");
        admin.remove(1);   // has votes → FAIL

        //  Close election + display final results

        system.closeElection();

        System.out.println("── Final Results (Polymorphism) ──");
        system.showResults();  


        // Try voting after close (should fail)

        System.out.println("── Vote After Close (should fail) ──");
        system.castVote("V004", "qwerty", 2);
    }
}
