package hotkitchen.service

import hotkitchen.models.Profile
import hotkitchen.models.Profiles
import hotkitchen.models.User
import hotkitchen.models.Users
import hotkitchen.models.Users.email
import hotkitchen.models.Users.password
import hotkitchen.models.Users.userType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserService {

    fun getAllUsers(): List<String> = transaction {
        Users.selectAll().map { it[Users.email] }
    }
    fun getUserByEmail(email: String): User? = transaction {
        Users.selectAll()
            .where { Users.email eq email }
            .map { User(it[Users.email], it[Users.userType], it[Users.password]) }
            .singleOrNull()
    }

    fun writeUserToDB(user: User) : Boolean{
        transaction {
            Users.insert {
                it[email] = user.email
                it[userType] = user.userType
                it[password] = user.password
            }
        }
        return getUserByEmail(user.email) != null
    }

    fun deleteUserFromDB(user: User) : Boolean {
        transaction {
            Users.deleteWhere { Users.email eq user.email }
        }
        return getUserByEmail(user.email) == null
    }

    fun deleteProfileFromDB(user: User) : Boolean{
        transaction {
            Profiles.deleteWhere { Profiles.email eq user.email }
        }
        return getProfileByEmail(user.email) == null
    }

    fun getProfileByEmail(email: String): Profile? = transaction {
        Profiles.selectAll()
            .where { Profiles.email eq email }
            .map { Profile(it[Profiles.name],
                it[Profiles.userType],
                it[Profiles.phone],
                it[Profiles.address],
                it[Profiles.email]) }
            .singleOrNull()
    }

    fun upsertProfileToDB(profile: Profile) {
        transaction {
            val updatedRows = Profiles.update({ Profiles.email eq profile.email }) {
                it[name] = profile.name
                it[userType] = profile.userType
                it[phone] = profile.phone
                it[address] = profile.address
            }
            if (updatedRows > 0) {
                true
            } else {
                Profiles.insert {
                    it[email] = profile.email
                    it[name] = profile.name
                    it[userType] = profile.userType
                    it[phone] = profile.phone
                    it[address] = profile.address
                }
                true
            }
        }
    }

}