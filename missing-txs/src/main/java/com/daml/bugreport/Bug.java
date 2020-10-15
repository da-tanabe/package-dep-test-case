package com.daml.bugreport;

import com.daml.ledger.api.v1.CommandsOuterClass.Command;
import com.daml.ledger.api.v1.CommandsOuterClass.Commands;
import com.daml.ledger.api.v1.CommandsOuterClass.CreateAndExerciseCommand;
import com.daml.ledger.api.v1.CommandsOuterClass.ExerciseByKeyCommand;
import com.daml.ledger.api.v1.LedgerOffsetOuterClass.LedgerOffset;
import com.daml.ledger.api.v1.LedgerOffsetOuterClass.LedgerOffset.LedgerBoundary;
import com.daml.ledger.api.v1.TransactionFilterOuterClass.Filters;
import com.daml.ledger.api.v1.TransactionFilterOuterClass.TransactionFilter;

import java.util.UUID;

import com.daml.ledger.api.v1.CommandServiceOuterClass.SubmitAndWaitRequest;
import com.daml.ledger.api.v1.ValueOuterClass.Identifier;
import com.daml.ledger.api.v1.ValueOuterClass.Record;
import com.daml.ledger.api.v1.ValueOuterClass.RecordField;
import com.daml.ledger.api.v1.ValueOuterClass.Value;
import com.daml.ledger.api.v1.TransactionServiceOuterClass.GetTransactionsRequest;

public class Bug {

    private static final Identifier NO_OP_IDENTIFIER = Identifier.newBuilder()
        .setPackageId("d395f1de549800308d2f6797482f824c2576867f546c324565a9a644aa63abb7")
        .setModuleName("Main")
        .setEntityName("NoOp")
        .build();

    private static final String APPLICATION_ID = "bugreport";
    private final String party;
    private final String ledgerId;

    public Bug(String party, String ledgerId) {
        this.party = party;
        this.ledgerId = ledgerId;
    }

    public SubmitAndWaitRequest firstRequest() {
        Commands commands = Commands.newBuilder()
            .setApplicationId(APPLICATION_ID)
            .setLedgerId(this.ledgerId)
            .setParty(this.party)
            .setCommandId(UUID.randomUUID().toString())
            .addCommands(Command.newBuilder()
                .setCreateAndExercise(
                    CreateAndExerciseCommand.newBuilder()
                        .setTemplateId(NO_OP_IDENTIFIER)
                        .setCreateArguments(Record.newBuilder().addFields(RecordField.newBuilder().setValue(
                            Value.newBuilder().setParty(this.party))))
                        .setChoice("DoNothing")
                        .setChoiceArgument(Value.newBuilder().setRecord(Record.getDefaultInstance()))

                ))
            .build();

        return SubmitAndWaitRequest.newBuilder().setCommands(commands).build();
    }

    public SubmitAndWaitRequest secondRequest() {
        Commands commands = Commands.newBuilder()
            .setApplicationId(APPLICATION_ID)
            .setLedgerId(this.ledgerId)
            .setParty(this.party)
            .setCommandId(UUID.randomUUID().toString())
            .addCommands(Command.newBuilder()
                .setExerciseByKey(
                    ExerciseByKeyCommand.newBuilder()
                        .setContractKey(Value.newBuilder().setParty(this.party))
                        .setTemplateId(NO_OP_IDENTIFIER)
                        .setChoice("DoNothing")
                        .setChoiceArgument(Value.newBuilder().setRecord(Record.getDefaultInstance()))

                ))
            .build();

        return SubmitAndWaitRequest.newBuilder().setCommands(commands).build();
    }

    public SubmitAndWaitRequest thirdRequest() {
        Commands commands = Commands.newBuilder()
            .setApplicationId(APPLICATION_ID)
            .setLedgerId(this.ledgerId)
            .setParty(this.party)
            .setCommandId(UUID.randomUUID().toString())
            .addCommands(Command.newBuilder()
                .setExerciseByKey(
                    ExerciseByKeyCommand.newBuilder()
                        .setContractKey(Value.newBuilder().setParty(this.party))
                        .setTemplateId(NO_OP_IDENTIFIER)
                        .setChoice("DoSomething")
                        .setChoiceArgument(Value.newBuilder().setRecord(Record.getDefaultInstance()))

                ))
            .build();

        return SubmitAndWaitRequest.newBuilder().setCommands(commands).build();
    }

    public GetTransactionsRequest allTransactions() {
        return GetTransactionsRequest.newBuilder()
            .setLedgerId(this.ledgerId)
            .setFilter(TransactionFilter.newBuilder().putFiltersByParty(this.party, Filters.getDefaultInstance()))
            .setBegin(LedgerOffset.newBuilder().setBoundary(LedgerBoundary.LEDGER_BEGIN))
            .setEnd(LedgerOffset.newBuilder().setBoundary(LedgerBoundary.LEDGER_END))
            .build();
    }
}
