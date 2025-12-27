package com.apkharsh.paymentLogger.ledger.repository;

import com.apkharsh.paymentLogger.ledger.dto.LedgerResponse;
import com.apkharsh.paymentLogger.ledger.entity.Ledger;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerRepository extends MongoRepository<Ledger, String> {

    Optional<List<Ledger>> findByPayerId(String payeeId);

    // ⭐ Option 1: Aggregation - Returns LedgerResponse directly
    @Aggregation(pipeline = {
            "{ $match: { $or: [ { payerId: ?0 }, { payeeId: ?0 } ] } }",
            "{ $lookup: { from: 'users', localField: 'payerId', foreignField: '_id', as: 'payer' } }",
            "{ $lookup: { from: 'users', localField: 'payeeId', foreignField: '_id', as: 'payee' } }",
            "{ $unwind: { path: '$payer', preserveNullAndEmptyArrays: true } }",
            "{ $unwind: { path: '$payee', preserveNullAndEmptyArrays: true } }",
            "{ $sort: { timestamp: -1 } }",
            // ⭐ Project to match LedgerResponse.UserInfo structure
            """
            { $project: {
                id: '$_id',
                payerId: 1,
                payeeId: 1,
                amount: 1,
                timestamp: 1,
                description: 1,
                payer: {
                    id: '$payer._id',
                    name: '$payer.name',
                    email: '$payer.email'
                },
                payee: {
                    id: '$payee._id',
                    name: '$payee.name',
                    email: '$payee.email'
                }
            }}
            """
    })
    List<LedgerResponse> findAllLedgersWithUsersByUserId(String userId);
}
