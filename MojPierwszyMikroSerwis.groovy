@RestController
class MojPierwszyMikroSerwis {

    @RequestMapping("/{name}")
    def hello(@PathVariable("name") String name) {
        return { message: 'Hello ' + name }
    }
}
