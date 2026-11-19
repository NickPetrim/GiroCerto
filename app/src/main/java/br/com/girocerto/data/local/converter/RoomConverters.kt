package br.com.girocerto.data.local.converter

import androidx.room.TypeConverter
import br.com.girocerto.domain.model.EmergencyChannel
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.TransactionType

class RoomConverters {

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromPlatform(value: Platform?): String? = value?.name

    @TypeConverter
    fun toPlatform(value: String?): Platform? = value?.let(Platform::valueOf)

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory?): String? = value?.name

    @TypeConverter
    fun toExpenseCategory(value: String?): ExpenseCategory? = value?.let(ExpenseCategory::valueOf)

    @TypeConverter
    fun fromEmergencyChannel(value: EmergencyChannel): String = value.name

    @TypeConverter
    fun toEmergencyChannel(value: String): EmergencyChannel = EmergencyChannel.valueOf(value)

    @TypeConverter
    fun fromForumCategory(value: ForumCategory): String = value.name

    @TypeConverter
    fun toForumCategory(value: String): ForumCategory = ForumCategory.valueOf(value)
}