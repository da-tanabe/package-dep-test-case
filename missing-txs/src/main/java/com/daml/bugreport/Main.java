// Copyright (c) 2020 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.bugreport;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;

import com.daml.ledger.api.v1.*;
import com.daml.ledger.api.v1.CommandServiceOuterClass.SubmitAndWaitForTransactionIdResponse;
import com.daml.ledger.api.v1.TransactionServiceOuterClass.GetTransactionsResponse;


public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String PARTY = "party";


    public static void main(String[] args) {
        final NettyChannelBuilder builder = NettyChannelBuilder.forAddress("localhost", 6865).usePlaintext();
        final ManagedChannel channel = builder.build();

        String ledgerId = getLedgerId(channel);
        Bug bug = new Bug(PARTY, ledgerId);

        CommandServiceGrpc.CommandServiceBlockingStub commandService = CommandServiceGrpc.newBlockingStub(channel);
        SubmitAndWaitForTransactionIdResponse response1 = commandService.submitAndWaitForTransactionId(bug.firstRequest());
        SubmitAndWaitForTransactionIdResponse response2 = commandService.submitAndWaitForTransactionId(bug.secondRequest());
        SubmitAndWaitForTransactionIdResponse response3 = commandService.submitAndWaitForTransactionId(bug.thirdRequest());

        logger.info("First transaction: {}", response1);
        logger.info("No-op transaction: {}", response2);
        logger.info("Third transaction: {}", response3);

        var transactionService = TransactionServiceGrpc.newBlockingStub(channel);
        Iterator<GetTransactionsResponse> iter = transactionService.getTransactions(bug.allTransactions());
        while (iter.hasNext()) {
            GetTransactionsResponse response = iter.next();
            logger.info("{}", response);
        }

        channel.shutdown();
    }

    private static String getLedgerId(ManagedChannel channel) {
        LedgerIdentityServiceGrpc.LedgerIdentityServiceBlockingStub stub = LedgerIdentityServiceGrpc.newBlockingStub(channel);
        var request = LedgerIdentityServiceOuterClass.GetLedgerIdentityRequest.getDefaultInstance();
        return stub.getLedgerIdentity(request).getLedgerId();
    }
}
