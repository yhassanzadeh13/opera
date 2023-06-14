package scenario.integrita.user;

/**
 * implementation of a mock user.
 */
public class UserWithTime extends User  implements BaseUser  {
  private int readTotalTime;
  private int writeTotalTime;
  private int updateStatusTotalTime;

  private int readCount;
  private int writeCount;
  private int updateStatusCount;


  @Override
  public int read(int from, int to) {
    int total = 0;
    int updateStatusTime = updateStatus();

    total += updateStatusTime;

    readTotalTime += total;
    readCount ++;
    return total;
  }

  @Override
  public int write(int opNum) {
    int total = 0;
    int updateStatusTime = updateStatus();

    total += updateStatusTime;


    writeTotalTime += total;
    writeCount ++;
    return  0;
  }

  @Override
  public int updateStatus() {
    /**
     * Inputs: @TODO Ut.Status
     * Outputs @TODO Ut.Status
     * 1. For j ∈ [1, N]
     * 2. Sj .Status = Sj .GetStatus() ⇐⇒ @ TODO N net connections
     * 3. Parse Sj .Status as ((p, l), σSj ) and add it to Q
     * 4. If Verif ySj (σSj , (p, l)) ̸= accept then Abort @TODO N *signature verifications
     * 5. If F (p, l) ̸= j then Abort @TODO N * F(p,l)
     * 6.(pmax,lmax)= maxQ j∈[1,N]
     * 7.(pmin,lmin)= min Q j∈[1,N]
     * 8. If (L(pmax, lmax) − L(pmin, lmin) ≥ N) then Abort
     * 9. p∗ = pmax l∗ = ⌈log(pmax)⌉ + 1
     * 10.(U′,(p∗,l∗),(τp∗,⊥,σU′),σ_S_F(p∗,l∗)) = S_F(p∗,l∗).Pull(Ut,(p∗,l∗)) ⇐⇒ @TODO 1 * net connection
     * 11.If VerifyU′(τp∗||p∗,σU′) = False then Abort @TODO 1 * signature verification
     * 12.If VerifySF(p∗,l∗)(τp∗||p∗,σSF(p∗,l∗)) = False then Abort @TODO 1 * signature verification
     * 13.(U′′,(p∗,0),(Np∗,0,⊥,σU′′),σSF(p∗,0)) = SF(p∗,0).Pull(Ut,(p∗,0)) ⇐⇒ @TODO 1 * net connection
     * 14.If VerifyU′′(Np∗,0||p∗,σU′′) = False then Abort @TODO 1 * signature verification
     * 15. If U′ != U′′ then Abort
     * 16. Retrieve τ_v from Ut.Status
     * 17. path = Nodes (pi , li ) along the incremental proof of τv and τp∗
     * 18. For (pi, li) ∈ path
     * 19. (Uk,(pi,li),(Npi,li,op,σUk),σSF(pi,li)) = SF(pi,li).Pull(Ut,pi,li)⇐⇒ @TODO X * net connections where X: The number of servers that hold the nodes along the the Incremental proof between Ut.Status and P*
     * 20. If Npi,li is a leaf
     * If  VerifyUk(Npi,li||pi,σUk) = False then Abort @TODO X * signature verification where X is the number of leaf nodes along the the Incremental proof between Ut.Status and P*
     * 21. proof.insert((pi , li ), Npi ,li , op)
     * 22. If IncVrfy(τv, τp∗ , proof) = False then Abort @TODO X * hash where X is the number of hash operations required to verify the incremental proof between Ut.Status and P*
     * 23. Ut.Status = (p∗, τp∗ , σSF (p∗,l∗) )
     * */
    int total = 0;
    // 2 * Net
    total += 2 * BenchMark.NetDelay;
    total += (BenchMark.TotalServers + 3)* BenchMark.SignVerify;
    total +=  BenchMark.TotalServers * BenchMark.Fpl; // p and l depend
    updateStatusTotalTime++;
    updateStatusCount ++;

//    this.status
    return total;
  }

  @Override
  public float getAvgReadTime() {
    return readTotalTime/readCount;
  }

  @Override
  public float getAvgWriteTime() {
    return writeTotalTime/writeCount;
  }

  @Override
  public float getAvgUpdateStatusTime() {
    return updateStatusTotalTime/updateStatusCount;
  }
}
