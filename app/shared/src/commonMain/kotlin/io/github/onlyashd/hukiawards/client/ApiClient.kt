package io.github.onlyashd.hukiawards.client

import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Routes.Api
import io.github.onlyashd.hukiawards.model.Routes.Server
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * High-level core HTTP network client wrapper responsible for transmitting requests
 * to the Ktor backend api layer.
 *
 * Automatically manages base route resolution and attaches JSON content type headers
 * alongside the current session's JWT bearer authentication credential signatures.
 *
 * @property client The underlying pre-configured Ktor engine instance.
 * @property token The raw authenticated JWT string parsed from the initial authentication workflow.
 */
class ApiClient(val client: HttpClient, val token: String) {
    /**
     * Complete fully qualified base URL path mapping calculated by combining server endpoints.
     * Ex: `http://localhost:8080/api`
     */
    val apiBase = Server.path + Api.path

    /**
     * Resolves a relative functional endpoint definition structure into a absolute URL location,
     * optionally embedding an identification resource path component.
     *
     * @param route The relative domain target destination config.
     * @param id An optional database identification value tag or unique UUID string.
     * @return The fully compiled complete absolute network path address destination.
     */
    fun resolve(route: Routes, id: String? = null): String {
        val path = if (id != null) "${route.path}/$id" else route.path
        return "$apiBase$path"
    }

    /**
     * Resolves an endpoint path definition paired with a standard URL encoded raw text query filter segment.
     *
     * @param route The relative domain target destination config.
     * @param query The unencoded query constraint string entered by the user.
     * @return The fully compiled complete absolute network path combined with query syntax.
     */
    fun resolveQuery(route: Routes, query: String): String {
        return "$apiBase${route.path}?q=$query"
    }

    /**
     * Resolves an administrative route by prefixing it with the standard admin path.
     */
    fun resolveAdmin(route: Routes, id: String? = null): String {
        val adminPath = Routes.Admin.path + route.path
        val path = if (id != null) "$adminPath/$id" else adminPath
        return "$apiBase$path"
    }

    /**
     * Asynchronously executes a standard raw, string-addressed HTTP GET request.
     * Handy for accessing static files, tracking external images, or custom outside endpoints.
     *
     * @param T The expected deserialized target data payload layout.
     * @param urlString The raw absolute URL destination resource.
     * @return The parsed object entity converted from response data by ContentNegotiation.
     */
    suspend inline fun <reified T> get(urlString: String): T {
        return client.get(urlString) {
            bearerAuth(token)
            accept(ContentType.Application.Json)
        }.body<T>()
    }

    /**
     * Requests a data structure model array or singular object targeting a typed application route.
     */
    suspend inline fun <reified T> get(route: Routes, id: String? = null): T {
        return client.get(resolve(route, id)) {
            bearerAuth(token)
            accept(ContentType.Application.Json)
        }.body<T>()
    }

    /**
     * Requests data records filtering results using URL parameters matching a specific text query string.
     */
    suspend inline fun <reified T> get(route: Routes, query: String): T {
        return client.get(resolveQuery(route, query)) {
            bearerAuth(token)
            accept(ContentType.Application.Json)
        }.body<T>()
    }

    /**
     * Downloads raw bytes from an endpoint. Useful for images.
     */
    suspend fun download(route: Routes, id: String? = null): ByteArray {
        return client.get(resolve(route, id)) {
            bearerAuth(token)
        }.body<ByteArray>()
    }

    /**
     * Transmits a payload data bundle to insert new records or execute actions on the server.
     *
     * @param T The expected server response acknowledgement format model.
     * @param route The backend route target designation configuration.
     * @param body The item object instance layout intended to be serialized to a JSON block.
     * @param isAdmin Whether to use the administrative route prefix.
     * @return Response structure metadata or confirmed object instance.
     */
    suspend inline fun <reified T> post(
        route: Routes,
        body: Any? = null,
        id: String? = null,
        isAdmin: Boolean = false
    ): T =
        client.post(if (isAdmin) resolveAdmin(route, id) else resolve(route, id)) {
            bearerAuth(token)
            if (body != null) {
                contentType(ContentType.Application.Json) // Fixes data payload interpretation drops
                setBody(body)
            }
        }.body()

    /**
     * Executes an in-place modification or update request on a specific, targeted data record.
     *
     * @param T The expected server response acknowledgement format model.
     * @param route The backend route target designation configuration.
     * @param id The persistent identification primary key value matching the server record.
     * @param body The modified state fields structure block container.
     * @param isAdmin Whether to use the administrative route prefix.
     * @return Response structure metadata or updated object instance.
     */
    suspend inline fun <reified T> put(
        route: Routes,
        id: String,
        body: Any,
        isAdmin: Boolean = false
    ): T =
        client.put(if (isAdmin) resolveAdmin(route, id) else resolve(route, id)) {
            bearerAuth(token)
            contentType(ContentType.Application.Json) // Fixes data payload interpretation drops
            setBody(body)
        }.body()

    /**
     * Requests the irreversible removal or destruction of a record entity on the server.
     *
     * @param T The expected server confirmation response structure layout.
     * @param route The backend route target designation configuration.
     * @param id The unique item key or entity string code being targeted for destruction.
     * @param isAdmin Whether to use the administrative route prefix.
     * @return Confirmation tracking block wrapper data.
     */
    suspend inline fun <reified T> delete(
        route: Routes,
        id: String? = null,
        isAdmin: Boolean = false
    ): T =
        client.delete(if (isAdmin) resolveAdmin(route, id) else resolve(route, id!!)) {
            bearerAuth(token)
        }.body()
}
