package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.resources.ContextResource;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ContextAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextServiceTest")
public class ContextServiceTest {

    //Vamos mockar todas as classes, porem, o service será injetado
    @Mock
    private ContextRepository contextRepository;
    @Mock
    private JWTService jwtService;

    @InjectMocks
    private ContextService service;

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().buildContextRegisterDTO();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final Optional<Context> contextOptional = ContextBuilder.anContext().buildOptionalContext();
    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();

    @Test
    public void insertAContextTest() throws ContextAlreadyExistsException, InvalidUserException , ObjectNotFoundException {

        ContextDTO response = service.insert(String.valueOf(this.jwtService.authenticate(userLoginDTO)), this.contextRegisterDTO);

        assertEquals(response.getName(),this.contextRegisterDTO.getName());
        assertEquals(response.getImageUrl(),this.contextRegisterDTO.getImageUrl());
        assertEquals(response.getSoundUrl(),this.contextRegisterDTO.getSoundUrl());
        assertEquals(response.getVideoUrl(),this.contextRegisterDTO.getVideoUrl());
    }

    //questão dos tokens, como usar pageable no construtor o que é response

    //OBS: os tokens são de User né?
    //OBS 2: construtor precisa de (Token e contextRegisterDTO)
    //OBS 3: como eu puxo o token para construtor insert?
    // Todo: colocar tokens em variável de ambiente

    @Test
    public void insertAContextAlreadyExistTest(){
        // ** IgnoreCase(Define as "strings" como iguais quando a diferença entre as letras são somente o fato de serem minusculas ou maiusculas **
        // OBS: como vou introduzir o parâmetro pageable? ja que o construtor é (String nome, Pageable)

        Mockito.when(this.contextRepository.findAllByNameStartsWithIgnoreCase
                        (this.contextRegisterDTO.getName(), Pageable)
                .thenReturn(this.contextOptional));

        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            service.insert(String.valueOf(this.jwtService.authenticate(userLoginDTO)), this.contextRegisterDTO);
        });

        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
    }

//    @Test
//    public void updateAContextTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), pageable?? ));
//    }
//    @Test
//    public void deleteAContextTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), pageable ));
//    }
//    @Test
//    public void findContextByParametersTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), this.contextRegisterDTO.get ));
//    }
//    @Test
//    public void findContextByCreatorTest(){
//        Mockito.when(this.contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(this.contextRegisterDTO.getName(), pageable));
//    }
//    @Test
//    public void validateUserTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase()this.contextRegisterDTO.getName(), pageable);
//    }

    //medir cobertura dos testes
    //falta fazer os testes, remoção e atualização

}
