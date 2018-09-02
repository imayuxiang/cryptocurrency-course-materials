import java.util.ArrayList;
import java.util.*;
import java.util.Map.Entry;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool pool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.pool = new UTXOPool(utxoPool);
    }

    public UTXOPool  getUTXOPool(){
        return this.pool;
    }
    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS

        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        HashSet<UTXO> utxoSet = new HashSet<>();
        double sumOfInputVals = 0, sumOfOutputVals = 0;
        //Crypto crypto  = new Crypto(); verifySignature(PublicKey pubKey, byte[] message, byte[] signature)

        for (int i = 0; i < inputs.size(); i++) {
            Transaction.Input in = inputs.get(i);
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output o = this.pool.getTxOutput(u);
            // check 1 poll contains all outputs
            if (!pool.contains(u)){
                return false;
            }

            // check2 signature is valid
            if (in.signature == null || !Crypto.verifySignature(o.address, tx.getRawDataToSign(i), in.signature)){
                return false;
            }


            utxoSet.add(u);
            sumOfInputVals += o.value;
        }

        //check3  no UTXO is claimed mutiple times by tx
        if (utxoSet.size() != inputs.size()){
            return false;
        }

        for (int i=0; i<outputs.size(); i++){
                sumOfOutputVals += outputs.get(i).value;
        //  check4 output values are non-negtive
                if (outputs.get(i).value  < 0){
                    return false;
                }
        }

        // sumofinput values greater than or equal sum of ouput values
        if (sumOfInputVals < sumOfOutputVals){
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        //Transaction[] transactions = new Array();
        if (possibleTxs == null){
            return new Transaction[0];
        }
        ArrayList<Transaction> validTxs = new ArrayList<>();
        for (Transaction tx : possibleTxs){
            if (isValidTx(tx)){
                validTxs.add(tx);
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    this.pool.removeUTXO(utxo);
                }
               
                byte[] txHash = tx.getHash(); 
                int index = 0;
                for (Transaction.Output output : tx.getOutputs()) {
                    UTXO utxo = new UTXO(txHash, index);
                    index += 1;
                    this.pool.addUTXO(utxo, output);
                }
            }
        }
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }

}
